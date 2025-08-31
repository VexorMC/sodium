package net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks;

import dev.vexor.radium.compat.mojang.minecraft.WorldUtil;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortType;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.PresentTranslucentData;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.TranslucentData;
import net.caffeinemc.mods.sodium.client.util.BlockRenderType;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import org.joml.Vector3dc;

import java.util.Map;

/**
 * Rebuilds all the meshes of a chunk for each given render pass with non-occluded blocks. The result is then uploaded
 * to graphics memory on the main thread.
 * <p>
 * This task takes a slice of the level from the thread it is created on. Since these slices require rather large
 * array allocations, they are pooled to ensure that the garbage collector doesn't become overloaded.
 */
public class ChunkBuilderMeshingTask extends ChunkBuilderTask<ChunkBuildOutput> {
    private final ChunkRenderContext renderContext;

    public ChunkBuilderMeshingTask(RenderSection render, int buildTime, Vector3dc absoluteCameraPos, ChunkRenderContext renderContext) {
        super(render, buildTime, absoluteCameraPos);
        this.renderContext = renderContext;
    }

    @Override
    public ChunkBuildOutput execute(ChunkBuildContext buildContext, CancellationToken cancellationToken) {
        Profiler profiler = MinecraftClient.getInstance().profiler;
        BuiltSectionInfo.Builder renderData = new BuiltSectionInfo.Builder();
        ChunkOcclusionDataBuilder occluder = new ChunkOcclusionDataBuilder();

        ChunkBuildBuffers buffers = buildContext.buffers;
        buffers.init(renderData, this.render.getSectionIndex());

        BlockRenderCache cache = buildContext.cache;
        cache.init(this.renderContext);

        LevelSlice slice = cache.getWorldSlice();

        int minX = this.render.getOriginX();
        int minY = this.render.getOriginY();
        int minZ = this.render.getOriginZ();

        int maxX = minX + 16;
        int maxY = minY + 16;
        int maxZ = minZ + 16;

        // Initialise with minX/minY/minZ so initial getBlockState crash context is correct
        BlockPos.Mutable blockPos = new BlockPos.Mutable(minX, minY, minZ);
        BlockPos.Mutable modelOffset = new BlockPos.Mutable();

        TranslucentGeometryCollector collector = null;
        if (SodiumClientMod.options().performance.getSortBehavior() != SortBehavior.OFF) {
            collector = new TranslucentGeometryCollector(this.render.getPosition());
        }
        BlockRenderContext context = new BlockRenderContext(slice, collector);

        profiler.push("render blocks");
        try {
            for (int y = minY; y < maxY; y++) {
                if (cancellationToken.isCancelled()) {
                    return null;
                }

                for (int z = minZ; z < maxZ; z++) {
                    for (int x = minX; x < maxX; x++) {
                        blockPos.setPosition(x, y, z);
                        var blockState = slice.getBlockState(blockPos);
                        var block = blockState.getBlock();
                        var blockType = block.getBlockType();

                        if (BlockRenderType.isInvisible(blockType) && !block.hasBlockEntity()) {
                            continue;
                        }

                        blockState = block.getBlockState(blockState, slice, blockPos);

                        modelOffset.setPosition(x & 15, y & 15, z & 15);

                        if (BlockRenderType.isModel(blockType)) {
                            BakedModel model = cache.getBlockModels()
                                    .getBakedModel(blockState);

                            context.update(blockPos, modelOffset, blockState, model);
                            cache.getBlockRenderer()
                                    .renderModel(context, buffers);
                        }

                        if (BlockRenderType.isLiquid(blockType)) {
                            cache.getFluidRenderer().render(slice, blockState, blockState, blockPos, modelOffset, collector, buffers);
                        }

                        if (block.hasBlockEntity()) {
                            BlockEntity entity = slice.getBlockEntity(blockPos);
                            if (entity != null) {
                                var renderer = BlockEntityRenderDispatcher.INSTANCE.getRenderer(entity);

                                if (renderer != null) {
                                    entity.setPos(new BlockPos(x, y, z));
                                    renderData.addBlockEntity(entity, false);
                                }
                            }
                        }

                        if (block.hasTransparency()) {
                            occluder.markClosed(blockPos);
                        }
                    }
                }
            }
        } catch (CrashException ex) {
            // Propagate existing crashes (add context)
            throw fillCrashInfo(ex.getReport(), slice, blockPos);
        } catch (Exception ex) {
            // Create a new crash report for other exceptions (e.g. thrown in getQuads)
            throw fillCrashInfo(CrashReport.create(ex, "Encountered exception while building chunk meshes"), slice, blockPos);
        }
        profiler.swap("mesh appenders");

        SortType sortType = SortType.NONE;
        if (collector != null) {
            sortType = collector.finishRendering();
        }

        // cancellation opportunity right before translucent sorting
        if (cancellationToken.isCancelled()) {
            profiler.pop();
            return null;
        }
        profiler.swap("translucency sorting");

        boolean reuseUploadedData = false;
        TranslucentData translucentData = null;
        if (collector != null) {
            var oldData = this.render.getTranslucentData();
            translucentData = collector.getTranslucentData(oldData, this);
            reuseUploadedData = translucentData == oldData;
        }

        Map<TerrainRenderPass, BuiltSectionMeshParts> meshes = new Reference2ReferenceOpenHashMap<>();
        var visibleSlices = DefaultChunkRenderer.getVisibleFaces(
                (int) this.absoluteCameraPos.x(), (int) this.absoluteCameraPos.y(), (int) this.absoluteCameraPos.z(),
                this.render.getChunkX(), this.render.getChunkY(), this.render.getChunkZ());

        if (translucentData != null && translucentData.meshesWereModified()) {
            meshes.put(DefaultTerrainRenderPasses.TRANSLUCENT, buffers.createModifiedTranslucentMesh(translucentData.getUpdatedQuads()));
            renderData.addRenderPass(DefaultTerrainRenderPasses.TRANSLUCENT);
        }

        for (TerrainRenderPass pass : DefaultTerrainRenderPasses.ALL) {
            if (meshes.containsKey(pass)) {
                continue;
            }

            // if the translucent geometry needs to share an index buffer between the directions,
            // consolidate all translucent geometry into UNASSIGNED
            boolean translucentBehavior = collector != null && pass.isTranslucent();
            boolean forceUnassigned = translucentBehavior && sortType.needsDirectionMixing;
            boolean sliceReordering = !translucentBehavior || sortType.allowSliceReordering;
            BuiltSectionMeshParts mesh = buffers.createMesh(pass, visibleSlices, forceUnassigned, sliceReordering);

            if (mesh != null) {
                meshes.put(pass, mesh);
                renderData.addRenderPass(pass);
            }
        }

        renderData.setOcclusionData(occluder.build());

        var output = new ChunkBuildOutput(this.render, this.submitTime, translucentData, renderData.build(), meshes);

        if (collector != null) {
            if (reuseUploadedData) {
                output.markAsReusingUploadedData();
            } else if (translucentData instanceof PresentTranslucentData present) {
                var sorter = present.getSorter();
                sorter.writeIndexBuffer(this, true);
                output.setSorter(sorter);
            }
        }

        profiler.pop();

        return output;
    }

    private CrashException fillCrashInfo(CrashReport report, LevelSlice slice, BlockPos pos) {
        CrashReportSection crashReportSection = report.addElement("Block being rendered", 1);

        crashReportSection.add("Chunk section", this.render);
        if (this.renderContext != null) {
            crashReportSection.add("Render context volume", this.renderContext.volume());
        }

        return new CrashException(report);
    }

    @Override
    public int getEffort() {
        return ChunkBuilder.HIGH_EFFORT;
    }
}