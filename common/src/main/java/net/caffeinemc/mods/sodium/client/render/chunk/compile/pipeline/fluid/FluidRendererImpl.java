package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.fluid;

import dev.vexor.radium.compat.mojang.minecraft.WorldUtil;
import dev.vexor.radium.util.FluidSprites;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class FluidRendererImpl extends FluidRenderer {
    private final ColorProviderRegistry colorProviderRegistry;
    private final DefaultFluidRenderer defaultRenderer;
    private final DefaultRenderContext defaultContext;

    private final FluidSprites sprites;

    public FluidRendererImpl(ColorProviderRegistry colorProviderRegistry, LightPipelineProvider lighters) {
        this.colorProviderRegistry = colorProviderRegistry;
        defaultRenderer = new DefaultFluidRenderer(lighters);
        defaultContext = new DefaultRenderContext();
        sprites = FluidSprites.create();
    }

    public void render(LevelSlice level, BlockState blockState, BlockState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkBuildBuffers buffers) {
        var material = DefaultMaterials.forFluidState(fluidState);
        var meshBuilder = buffers.get(material);
        var fluid = WorldUtil.getFluid(fluidState);

        defaultContext.setUp(this.colorProviderRegistry, false);

        defaultRenderer.render(level, blockState, blockPos, offset, collector, meshBuilder, material, defaultContext.getColorProvider(fluid), sprites.forFluid(fluid));

        defaultContext.clear();
    }

    private static class DefaultRenderContext {
        private ColorProviderRegistry colorProviderRegistry;
        private boolean hasModOverride;

        public void setUp(ColorProviderRegistry colorProviderRegistry, boolean hasModOverride) {
            this.colorProviderRegistry = colorProviderRegistry;
            this.hasModOverride = hasModOverride;
        }

        public void clear() {
            this.hasModOverride = false;
        }

        public ColorProvider<BlockState> getColorProvider(AbstractFluidBlock fluid) {
            var override = this.colorProviderRegistry.getColorProvider(fluid);

            if (!hasModOverride && override != null) {
                return override;
            }

            return FabricColorProvider.adapt();
        }
    }
}