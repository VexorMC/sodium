package net.caffeinemc.mods.sodium.client.render.chunk.terrain.material;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;

public class DefaultMaterials {
    public static final Material SOLID = new Material(DefaultTerrainRenderPasses.SOLID, AlphaCutoffParameter.ZERO, true);
    public static final Material CUTOUT = new Material(DefaultTerrainRenderPasses.CUTOUT, AlphaCutoffParameter.ONE_TENTH, false);
    public static final Material CUTOUT_MIPPED = new Material(DefaultTerrainRenderPasses.CUTOUT, AlphaCutoffParameter.HALF, true);
    public static final Material TRANSLUCENT = new Material(DefaultTerrainRenderPasses.TRANSLUCENT, AlphaCutoffParameter.ZERO, true);

    public static Material forBlockState(BlockState state) {
        return forRenderLayer(state.getBlock().getRenderLayerType());
    }

    public static Material forFluidState(BlockState state) {
        return forRenderLayer(state.getBlock().getRenderLayerType());
    }

    public static Material forRenderLayer(RenderLayer layer) {
        return switch (layer) {
            case SOLID -> SOLID;
            case CUTOUT -> CUTOUT;
            case CUTOUT_MIPPED -> CUTOUT_MIPPED;
            case TRANSLUCENT -> TRANSLUCENT;
        };
    }
}
