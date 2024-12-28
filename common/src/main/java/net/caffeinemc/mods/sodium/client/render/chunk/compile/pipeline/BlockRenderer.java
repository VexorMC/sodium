package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import dev.vexor.radium.compat.mojang.minecraft.random.SingleThreadedRandomSource;
import dev.vexor.radium.frapi.impl.renderer.VanillaModelEncoder;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import dev.vexor.radium.frapi.api.renderer.v1.material.BlendMode;
import dev.vexor.radium.frapi.api.renderer.v1.material.RenderMaterial;
import dev.vexor.radium.frapi.api.renderer.v1.material.ShadeMode;
import net.legacyfabric.fabric.api.util.TriState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BlockRenderer extends AbstractBlockRenderContext {
    private final ColorProviderRegistry colorProviderRegistry;
    private final int[] vertexColors = new int[4];
    private final ChunkVertexEncoder.Vertex[] vertices = ChunkVertexEncoder.Vertex.uninitializedQuad();

    private ChunkBuildBuffers buffers;

    private final Vector3f posOffset = new Vector3f();
    private final BlockPos.Mutable scratchPos = new BlockPos.Mutable();
    @Nullable
    private ColorProvider<BlockState> colorProvider;
    private TranslucentGeometryCollector collector;

    public BlockRenderer(ColorProviderRegistry colorRegistry, LightPipelineProvider lighters) {
        this.colorProviderRegistry = colorRegistry;
        this.lighters = lighters;

        this.random = new SingleThreadedRandomSource(42L);
    }

    public void prepare(ChunkBuildBuffers buffers, LevelSlice level, TranslucentGeometryCollector collector) {
        this.buffers = buffers;
        this.level = level;
        this.collector = collector;
        this.slice = level;
    }

    public void release() {
        this.buffers = null;
        this.level = null;
        this.collector = null;
        this.slice = null;
    }

    public void renderModel(BakedModel model, BlockState state, BlockPos pos, BlockPos origin) {
        this.state = state;
        this.pos = pos;

        this.randomSeed = 42L;

        this.posOffset.set(origin.getX(), origin.getY(), origin.getZ());

        var offsetType = state.getBlock().getOffsetType();

        if (offsetType != Block.OffsetType.NONE) {
            int x = origin.getX();
            int z = origin.getZ();
            // Taken from MathHelper.hashCode()
            long i = (x * 3129871L) ^ z * 116129781L;
            i = i * i * 42317861L + i * 11L;

            double fx = (((i >> 16 & 15L) / 15.0F) - 0.5f) * 0.5f;
            double fz = (((i >> 24 & 15L) / 15.0F) - 0.5f) * 0.5f;
            double fy = 0;

            if (offsetType == Block.OffsetType.XYZ) {
                fy += (((i >> 20 & 15L) / 15.0F) - 1.0f) * 0.2f;
            }

            posOffset.add((float) fx, (float) fy, (float) fz);
        }

        this.colorProvider = this.colorProviderRegistry.getColorProvider(state.getBlock());

        type = state.getBlock().getRenderLayerType();

        this.prepareCulling(true);
        this.prepareAoInfo(model.useAmbientOcclusion());

        VanillaModelEncoder.emitBlockQuads(getEmitter(), model, state, this::isFaceCulled);
    }

    /**
     * Process quad, after quad transforms and the culling check have been applied.
     */
    @Override
    protected void processQuad(MutableQuadViewImpl quad) {
        final RenderMaterial mat = quad.material();
        final TriState aoMode = mat.ambientOcclusion();
        final ShadeMode shadeMode = mat.shadeMode();
        final LightMode lightMode;
        if (aoMode == TriState.DEFAULT) {
            lightMode = this.defaultLightMode;
        } else {
            lightMode = this.useAmbientOcclusion && aoMode.get() ? LightMode.SMOOTH : LightMode.FLAT;
        }
        final boolean emissive = mat.emissive();

        Material material;

        final BlendMode blendMode = mat.blendMode();
        if (blendMode == BlendMode.DEFAULT) {
            material = DefaultMaterials.forRenderLayer(type);
        } else {
            material = DefaultMaterials.forRenderLayer(blendMode.blockRenderLayer == null ? type : blendMode.blockRenderLayer);
        }

        this.tintQuad(quad);
        this.shadeQuad(quad, lightMode, emissive, shadeMode);
        this.bufferQuad(quad, this.quadLightData, material);
    }

    private void tintQuad(MutableQuadViewImpl quad) {
        int tintIndex = quad.tintIndex();

        if (tintIndex != -1) {
            ColorProvider<BlockState> colorProvider = this.colorProvider;

            if (colorProvider != null) {
                int[] vertexColors = this.vertexColors;
                colorProvider.getColors(this.slice, this.pos, this.scratchPos, this.state, quad, vertexColors);

                for (int i = 0; i < 4; i++) {
                    quad.color(i, ColorMixer.mulComponentWise(vertexColors[i], quad.color(i)));
                }
            }
        }
    }

    private void bufferQuad(MutableQuadViewImpl quad, QuadLightData light, Material material) {
        // TODO: Find a way to reimplement quad reorientation
        ModelQuadOrientation orientation = ModelQuadOrientation.NORMAL;
        ChunkVertexEncoder.Vertex[] vertices = this.vertices;
        Vector3f offset = this.posOffset;

        for (int dstIndex = 0; dstIndex < 4; dstIndex++) {
            int srcIndex = orientation.getVertexIndex(dstIndex);

            ChunkVertexEncoder.Vertex out = vertices[dstIndex];
            out.x = quad.x(srcIndex) + offset.x;
            out.y = quad.y(srcIndex) + offset.y;
            out.z = quad.z(srcIndex) + offset.z;

            out.color = quad.color(srcIndex);
            out.ao = light.br[srcIndex];

            out.u = quad.u(srcIndex);
            out.v = quad.v(srcIndex);

            out.light = light.lm[srcIndex];
        }

        var materialBits = material.bits();
        ModelQuadFacing normalFace = quad.normalFace();

        // attempt render pass downgrade if possible
        var pass = material.pass;

        // collect all translucent quads into the translucency sorting system if enabled
        if (pass.isTranslucent() && this.collector != null) {
            this.collector.appendQuad(quad.getFaceNormal(), vertices, normalFace);
        }

        ChunkModelBuilder builder = this.buffers.get(pass);
        ChunkMeshBufferBuilder vertexBuffer = builder.getVertexBuffer(normalFace);
        vertexBuffer.push(vertices, materialBits);
    }
}