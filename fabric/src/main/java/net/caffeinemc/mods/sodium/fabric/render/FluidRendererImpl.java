package net.caffeinemc.mods.sodium.fabric.render;

import dev.lunasa.compat.mojang.blaze3d.vertex.VertexConsumer;
import dev.lunasa.compat.mojang.minecraft.WorldUtil;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.quad.blender.BlendedColorProvider;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.caffeinemc.mods.sodium.client.services.FluidRendererFactory;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.fabricmc.fabric.impl.renderer.SpriteFinderImpl;
import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class FluidRendererImpl extends FluidRenderer {
    private final ColorProviderRegistry colorProviderRegistry;
    private final DefaultFluidRenderer defaultRenderer;
    private final DefaultRenderContext defaultContext;

    public FluidRendererImpl(ColorProviderRegistry colorProviderRegistry, LightPipelineProvider lighters) {
        this.colorProviderRegistry = colorProviderRegistry;
        defaultRenderer = new DefaultFluidRenderer(lighters);
        defaultContext = new DefaultRenderContext();
    }

    public void render(LevelSlice level, BlockState blockState, BlockState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkBuildBuffers buffers) {
        var material = DefaultMaterials.forFluidState(fluidState);
        var meshBuilder = buffers.get(material);

        // Invoking FluidRenderHandler#renderFluid can invoke vanilla FluidRenderer#render.
        //
        // Sodium cannot let vanilla FluidRenderer#render run (during the invocation of FluidRenderHandler#renderFluid)
        // for two reasons.
        // 1. It is the hot path and vanilla FluidRenderer#render is not very fast.
        // 2. Fabric API's mixins to FluidRenderer#render expect it to be initially called from the chunk rebuild task,
        // not from inside FluidRenderHandler#renderFluid. Not upholding this assumption will result in all custom
        // geometry to be buffered twice.
        //
        // The default implementation of FluidRenderHandler#renderFluid invokes vanilla FluidRenderer#render, but
        // Fabric API does not support invoking vanilla FluidRenderer#render from FluidRenderHandler#renderFluid
        // directly and it does not support calling the default implementation of FluidRenderHandler#renderFluid (super)
        // more than once. Because of this, the parameters to vanilla FluidRenderer#render will be the same as those
        // initially passed to FluidRenderHandler#renderFluid, so they can be ignored.
        //
        // Due to all the above, Sodium injects into head of vanilla FluidRenderer#render before Fabric API and cancels
        // the call if it was invoked from inside FluidRenderHandler#renderFluid. The injector ends up calling
        // DefaultFluidRenderer#render, which emulates what vanilla FluidRenderer#render does, but is more efficient.
        // To allow invoking this method from the injector, where there is no local Sodium context, the renderer and
        // parameters are bundled into a DefaultRenderContext which is stored in a ThreadLocal.

        defaultContext.setUp(this.colorProviderRegistry, this.defaultRenderer, level, blockState, fluidState, blockPos, offset, collector, meshBuilder, material, false);

        Sprite[] lavaSprites = new Sprite[2];
        Sprite[] waterSprites = new Sprite[2];

        SpriteAtlasTexture spriteAtlasTexture = MinecraftClient.getInstance().getSpriteAtlasTexture();
        lavaSprites[0] = spriteAtlasTexture.getSprite("minecraft:blocks/lava_still");
        lavaSprites[1] = spriteAtlasTexture.getSprite("minecraft:blocks/lava_flow");
        waterSprites[0] = spriteAtlasTexture.getSprite("minecraft:blocks/water_still");
        waterSprites[1] = spriteAtlasTexture.getSprite("minecraft:blocks/water_flow");

        AbstractFluidBlock block = WorldUtil.getFluid(fluidState);
        Sprite[] sprites = block.getMaterial() == net.minecraft.block.material.Material.LAVA ? lavaSprites : waterSprites;

        // defaultRenderer.render(level, blockState, fluidState, blockPos, offset, collector, meshBuilder, material, colorProviderRegistry.getColorProvider(block), sprites);
    }

    private static class DefaultRenderContext {
        private DefaultFluidRenderer renderer;
        private LevelSlice level;
        private BlockState blockState;
        private BlockState fluidState;
        private BlockPos blockPos;
        private BlockPos offset;
        private TranslucentGeometryCollector collector;
        private ChunkModelBuilder meshBuilder;
        private Material material;
        private ColorProviderRegistry colorProviderRegistry;
        private boolean hasModOverride;

        public void setUp(ColorProviderRegistry colorProviderRegistry, DefaultFluidRenderer renderer, LevelSlice level, BlockState blockState, BlockState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkModelBuilder meshBuilder, Material material, boolean hasModOverride) {
            this.colorProviderRegistry = colorProviderRegistry;
            this.renderer = renderer;
            this.level = level;
            this.blockState = blockState;
            this.fluidState = fluidState;
            this.blockPos = blockPos;
            this.offset = offset;
            this.collector = collector;
            this.meshBuilder = meshBuilder;
            this.material = material;
            this.hasModOverride = hasModOverride;
        }

        public void clear() {
            this.renderer = null;
            this.level = null;
            this.blockState = null;
            this.fluidState = null;
            this.blockPos = null;
            this.offset = null;
            this.collector = null;
            this.meshBuilder = null;
            this.material = null;
            this.hasModOverride = false;
        }

        public ColorProvider<BlockState> getColorProvider(AbstractFluidBlock fluid) {
            var override = this.colorProviderRegistry.getColorProvider(fluid);

            if (!hasModOverride && override != null) {
                return override;
            }

            return FabricColorProviders.adapt();
        }
    }

    public static class FabricFactory implements FluidRendererFactory {
        @Override
        public FluidRenderer createPlatformFluidRenderer(ColorProviderRegistry colorRegistry, LightPipelineProvider lightPipelineProvider) {
            return new FluidRendererImpl(colorRegistry, lightPipelineProvider);
        }

        @Override
        public BlendedColorProvider<BlockState> getWaterColorProvider() {
            return new BlendedColorProvider<>() {
                @Override
                protected int getColor(LevelSlice slice, BlockState state, BlockPos pos) {
                    return BiomeColors.getWaterColor(slice, pos) | 0xFF000000;
                }
            };
        }

        @Override
        public BlendedColorProvider<BlockState> getWaterBlockColorProvider() {
            return new BlendedColorProvider<>() {
                @Override
                protected int getColor(LevelSlice slice, BlockState state, BlockPos pos) {
                    return BiomeColors.getWaterColor(slice, pos) | 0xFF000000;
                }
            };
        }
    }
}
