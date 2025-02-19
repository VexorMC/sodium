package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.BakedQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class BlockRenderer {
    private final ColorProviderRegistry colorProviderRegistry;
    private final BlockOcclusionCache occlusionCache;

    private final QuadLightData quadLightData = new QuadLightData();

    private final LightPipelineProvider lighters;

    private final ChunkVertexEncoder.Vertex[] vertices = ChunkVertexEncoder.Vertex.uninitializedQuad();

    private final boolean useAmbientOcclusion;

    private final int[] quadColors = new int[4];

    public BlockRenderer(ColorProviderRegistry colorRegistry, LightPipelineProvider lighters) {
        this.colorProviderRegistry = colorRegistry;
        this.lighters = lighters;

        this.occlusionCache = new BlockOcclusionCache();
        this.useAmbientOcclusion = MinecraftClient.isAmbientOcclusionEnabled();
    }

    public void renderModel(BlockRenderContext ctx, ChunkBuildBuffers buffers) {
        var material = DefaultMaterials.forBlockState(ctx.state());
        var meshBuilder = buffers.get(material);

        ColorProvider colorizer = this.colorProviderRegistry.getColorProvider(ctx.state().getBlock());

        LightPipeline lighter = this.lighters.getLighter(this.getLightingMode(ctx.state(), ctx.model()));
        Vec3d renderOffset = new Vec3d(0, 0, 0);

        var offsetType = ctx.state().getBlock().getOffsetType();

        if (offsetType != Block.OffsetType.NONE) {
            int x = (int) ctx.origin().x();
            int z = (int) ctx.origin().z();
            // Taken from MathHelper.hashCode()
            long i = (x * 3129871L) ^ z * 116129781L;
            i = i * i * 42317861L + i * 11L;

            double fx = (((i >> 16 & 15L) / 15.0F) - 0.5f) * 0.5f;
            double fz = (((i >> 24 & 15L) / 15.0F) - 0.5f) * 0.5f;
            double fy = 0;

            if (offsetType == Block.OffsetType.XYZ) {
                fy += (((i >> 20 & 15L) / 15.0F) - 1.0f) * 0.2f;
            }

            renderOffset.add((float) fx, (float) fy, (float) fz);
        }

        for (Direction face : DirectionUtil.ALL_DIRECTIONS) {
            List<BakedQuad> quads = this.getGeometry(ctx, face);

            if (!quads.isEmpty() && this.isFaceVisible(ctx, face)) {
                this.renderQuadList(ctx, material, lighter, colorizer, renderOffset, meshBuilder, quads, face);
            }
        }

        List<BakedQuad> all = this.getGeometry(ctx, null);

        if (!all.isEmpty()) {
            this.renderQuadList(ctx, material, lighter, colorizer, renderOffset, meshBuilder, all, null);
        }
    }

    private List<BakedQuad> getGeometry(BlockRenderContext ctx, Direction face) {
        var model = ctx.model();
        return face == null ? model.getQuads() : model.getByDirection(face);
    }

    private boolean isFaceVisible(BlockRenderContext ctx, Direction face) {
        return this.occlusionCache.shouldDrawSide(ctx.slice(), ctx.pos(), face);
    }

    private void renderQuadList(BlockRenderContext ctx, Material material, LightPipeline lighter, ColorProvider colorizer, Vec3d offset,
                                ChunkModelBuilder builder, List<BakedQuad> quads, Direction cullFace) {

        // This is a very hot allocation, iterate over it manually
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0, quadsSize = quads.size(); i < quadsSize; i++) {
            BakedQuadView quad = (BakedQuadView) quads.get(i);

            final var lightData = this.getVertexLight(ctx, lighter, cullFace, quad);
            final var vertexColors = this.getVertexColors(ctx, colorizer, quad);

            this.writeGeometry(ctx, builder, offset, material, quad, vertexColors, lightData);

            Sprite sprite = quad.getSprite();

            if (sprite != null) {
                builder.addSprite(sprite);
            }
        }
    }

    private QuadLightData getVertexLight(BlockRenderContext ctx, LightPipeline lighter, Direction cullFace, BakedQuadView quad) {
        QuadLightData light = this.quadLightData;
        lighter.calculate(quad, ctx.pos(), light, cullFace, quad.getLightFace(), quad.hasShade(), false);

        return light;
    }

    private int[] getVertexColors(BlockRenderContext ctx, ColorProvider colorProvider, BakedQuadView quad) {
        final int[] vertexColors = this.quadColors;

        if (colorProvider != null && quad.hasColor()) {
            colorProvider.getColors(ctx.slice(), ctx.state(), quad, vertexColors, ctx.pos());
        } else {
            Arrays.fill(vertexColors, 0xFFFFFF);
        }

        return vertexColors;
    }

    private void writeGeometry(BlockRenderContext ctx,
                               ChunkModelBuilder builder,
                               Vec3d offset,
                               Material material,
                               BakedQuadView quad,
                               int[] colors,
                               QuadLightData light)
    {
        ModelQuadOrientation orientation = ModelQuadOrientation.orientByBrightness(light.br, light.lm);
        var vertices = this.vertices;

        ModelQuadFacing normalFace = quad.getNormalFace();

        for (int dstIndex = 0; dstIndex < 4; dstIndex++) {
            int srcIndex = orientation.getVertexIndex(dstIndex);

            var out = vertices[dstIndex];
            out.x = ctx.origin().x() + quad.getX(srcIndex) + (float) offset.x;
            out.y = ctx.origin().y() + quad.getY(srcIndex) + (float) offset.y;
            out.z = ctx.origin().z() + quad.getZ(srcIndex) + (float) offset.z;

            out.color = ColorARGB.toABGR(colors[srcIndex]) | 0xFF000000;
            out.ao = light.br[srcIndex];

            out.u = quad.getTexU(srcIndex);
            out.v = quad.getTexV(srcIndex);

            out.light = light.lm[srcIndex];
        }

        if (material.isTranslucent() && ctx.collector != null) {
            ctx.collector.appendQuad(quad.getFaceNormal(), vertices, normalFace);
        }

        var vertexBuffer = builder.getVertexBuffer(normalFace);
        vertexBuffer.push(vertices, material);
    }

    private LightMode getLightingMode(BlockState state, BakedModel model) {
        if (this.useAmbientOcclusion && model.useAmbientOcclusion() && state.getBlock().getLightLevel() == 0) {
            return LightMode.SMOOTH;
        } else {
            return LightMode.FLAT;
        }
    }
}