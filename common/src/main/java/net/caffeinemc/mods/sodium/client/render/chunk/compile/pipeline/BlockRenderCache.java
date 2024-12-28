package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import dev.vexor.radium.compat.mojang.minecraft.BlockColors;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.ArrayLightDataCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.fluid.FluidRendererImpl;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.fluid.FluidRenderer;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModelShapes;
import net.minecraft.client.world.ClientWorld;

public class BlockRenderCache {
    private final ArrayLightDataCache lightDataCache;

    private final BlockRenderer blockRenderer;
    private final FluidRenderer fluidRenderer;

    private final BlockModelShapes blockModels;
    private final LevelSlice levelSlice;

    public BlockRenderCache(MinecraftClient minecraft, ClientWorld level) {
        this.levelSlice = new LevelSlice(level);
        this.lightDataCache = new ArrayLightDataCache(this.levelSlice);

        LightPipelineProvider lightPipelineProvider = new LightPipelineProvider(this.lightDataCache);

        var colorRegistry = new ColorProviderRegistry(BlockColors.INSTANCE);

        this.blockRenderer = new BlockRenderer(colorRegistry, lightPipelineProvider);
        this.fluidRenderer = new FluidRendererImpl(colorRegistry, lightPipelineProvider);

        this.blockModels = minecraft.getBlockRenderManager().getModels();
    }

    public BlockModelShapes getBlockModels() {
        return this.blockModels;
    }

    public BlockRenderer getBlockRenderer() {
        return this.blockRenderer;
    }

    public FluidRenderer getFluidRenderer() {
        return this.fluidRenderer;
    }

    public void init(ChunkRenderContext context) {
        this.lightDataCache.reset(context.origin());
        this.levelSlice.copyData(context);
    }

    public LevelSlice getWorldSlice() {
        return this.levelSlice;
    }

    public void cleanup() {
        this.levelSlice.reset();
    }
}
