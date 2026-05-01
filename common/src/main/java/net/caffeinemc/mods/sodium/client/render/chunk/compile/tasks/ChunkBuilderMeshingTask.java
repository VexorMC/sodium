package net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.estimation.MeshTaskSizeEstimator;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.DirectionalVisGraph;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortType;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.DynamicData;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.PresentTranslucentData;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.TranslucentData;
import net.caffeinemc.mods.sodium.client.util.BlockRenderType;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.level.LevelGeneratorType;
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
    private final SortBehavior sortBehavior;
    private final boolean forceSort;
    private final boolean blockingTask;

    public ChunkBuilderMeshingTask(RenderSection render, int buildTime, Vector3dc absoluteCameraPos, ChunkRenderContext renderContext, SortBehavior sortBehavior, boolean forceSort, boolean blockingTask) {
        super(render, buildTime, absoluteCameraPos);
        this.renderContext = renderContext;
        this.sortBehavior = sortBehavior;
        this.forceSort = forceSort;
        this.blockingTask = blockingTask;
    }

    @Override
    public ChunkBuildOutput execute(ChunkBuildContext buildContext, CancellationToken cancellationToken) {
        Profiler profiler = MinecraftClient.getInstance().profiler;
        BuiltSectionInfo.Builder renderData = new BuiltSectionInfo.Builder();
        DirectionalVisGraph occluder = new DirectionalVisGraph();

        ChunkBuildBuffers buffers = buildContext.buffers;
        buffers.init(renderData, this.section.getSectionIndex());

        BlockRenderCache cache = buildContext.cache;
        cache.init(this.renderContext);

        LevelSlice slice = cache.getWorldSlice();

        int minX = this.section.getOriginX();
        int minY = this.section.getOriginY();
        int minZ = this.section.getOriginZ();

        int maxX = minX + 16;
        int maxY = minY + 16;
        int maxZ = minZ + 16;

        // Initialise with minX/minY/minZ so initial getBlockState crash context is correct
        BlockPos.Mutable blockPos = new BlockPos.Mutable(minX, minY, minZ);
        BlockPos.Mutable modelOffset = new BlockPos.Mutable();

        boolean sortEnabled = this.sortBehavior != SortBehavior.OFF;
        TranslucentGeometryCollector collector = null;
        if (sortEnabled) {
            collector = new TranslucentGeometryCollector(this.section.getPosition(), this.sortBehavior);
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

                        if (blockType == BlockRenderType.INVISIBLE && !block.hasBlockEntity()) {
                            continue;
                        }

                        // required for doors, fences, tripwires, etc.
                        // see: BlockRenderManager#getModel
                        if (slice.getGeneratorType() != LevelGeneratorType.DEBUG) {
                            blockState = block.getBlockState(blockState, slice, blockPos);
                        }

                        int localX = x & 15;
                        int localY = y & 15;
                        int localZ = z & 15;
                        modelOffset.setPosition(localX, localY, localZ);

                        if (blockType == BlockRenderType.MODEL) {
                            var renderer = cache.getBlockRenderer();

                            renderer.prepare(buffers);

                            BakedModel model = cache.getBlockModels()
                                    .getBakedModel(blockState);

                            context.update(blockPos, modelOffset, blockState, model);

                            renderer.renderModel(context);
                        }

                        if (blockType == BlockRenderType.LIQUID) {
                            cache.getFluidRenderer().render(slice, blockState, blockState, blockPos, modelOffset, collector, buffers);
                        }

                        if (block.hasBlockEntity()) {
                            BlockEntity entity = slice.getBlockEntity(blockPos);

                            if (entity != null) {
                                var renderer = BlockEntityRenderDispatcher.INSTANCE.getRenderer(entity);

                                if (renderer != null) {
                                    renderData.addBlockEntity(entity, !renderer.rendersOutsideBoundingBox());
                                }
                            }
                        }

                        if (block.hasTransparency()) {
                            occluder.setOpaque(localX, localY, localZ);
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
        if (sortEnabled) {
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
        if (sortEnabled) {
            TranslucentData oldData = this.section.getTranslucentData();

            // Reusing non-dynamic data leads to attempting to sort with it again,
            // which throws an exception since it can only generate a sorter once.
            // To prevent this, reusing data is prevented when forceSort is enabled and the data is not dynamic.
            if (this.forceSort && !(oldData instanceof DynamicData)) {
                oldData = null;
            }

            translucentData = collector.getTranslucentData(oldData, this);
            reuseUploadedData = !this.forceSort && translucentData == oldData;
        }

        profiler.swap("meshing");

        Map<TerrainRenderPass, BuiltSectionMeshParts> meshes = new Reference2ReferenceOpenHashMap<>();
        var visibleSlices = DefaultChunkRenderer.getVisibleFaces(
                (int) this.absoluteCameraPos.x(), (int) this.absoluteCameraPos.y(), (int) this.absoluteCameraPos.z(),
                this.section.getChunkX(), this.section.getChunkY(), this.section.getChunkZ());

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
            boolean translucentBehavior = sortEnabled && pass.isTranslucent();
            boolean forceUnassigned = translucentBehavior && sortType.needsDirectionMixing;
            boolean sliceReordering = !translucentBehavior || sortType.allowSliceReordering;
            BuiltSectionMeshParts mesh = buffers.createMesh(pass, visibleSlices, forceUnassigned, sliceReordering);

            if (mesh != null) {
                meshes.put(pass, mesh);
                renderData.addRenderPass(pass);
            }
        }

        renderData.setOcclusionData(occluder.resolve());

        var output = new ChunkBuildOutput(this.section, this.submitTime, translucentData, renderData.build(), meshes, blockingTask);

        if (sortEnabled) {
            if (reuseUploadedData) {
                output.markAsNotContainingNewIndexData();
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

        crashReportSection.add("Chunk section", this.section);
        if (this.renderContext != null) {
            crashReportSection.add("Render context volume", this.renderContext.volume());
        }

        return new CrashException(report);
    }

    @Override
    public long estimateTaskSizeWith(MeshTaskSizeEstimator estimator) {
        return estimator.estimateSize(this.section);
    }
}