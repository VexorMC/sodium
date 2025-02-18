package dev.vexor.radium.util;

import dev.vexor.radium.mixin.sodium.core.access.ABlockRenderManager;
import dev.vexor.radium.mixin.sodium.core.access.AFluidRenderer;
import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;

/**
 * Caches fluid sprites and quickly allows you to access them for maximum efficiency.
 *
 * @author Lunasa
 */
public class FluidSprites {
    private final Sprite[] waterSprites;
    private final Sprite[] lavaSprites;

    public FluidSprites(Sprite[] waterSprites, Sprite[] lavaSprites) {
        this.waterSprites = waterSprites;
        this.lavaSprites = lavaSprites;
    }

    public Sprite[] forFluid(AbstractFluidBlock fluidBlock) {
        if (fluidBlock.getMaterial() == Material.WATER) return this.waterSprites;
        return this.lavaSprites;
    }

    public static FluidSprites create() {
        return new FluidSprites(getFluidRenderer().getWaterSprites(), getFluidRenderer().getLavaSprites());
    }

    private static AFluidRenderer getFluidRenderer() {
        return (AFluidRenderer) ((ABlockRenderManager) MinecraftClient.getInstance().getBlockRenderManager()).getFluidRenderer();
    }
}
