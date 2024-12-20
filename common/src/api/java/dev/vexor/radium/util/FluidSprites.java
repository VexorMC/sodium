package dev.vexor.radium.util;

import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;

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
        return new FluidSprites(getSpritesFor("water"), getSpritesFor("lava"));
    }

    // There is definitely a better way to do this...
    private static Sprite[] getSpritesFor(String fluidName) {
        SpriteAtlasTexture spriteAtlasTexture = MinecraftClient.getInstance().getSpriteAtlasTexture();

        Sprite[] sprites = new Sprite[2];

        sprites[0] = spriteAtlasTexture.getSprite("minecraft:blocks/%s_still".formatted(fluidName));
        sprites[1] = spriteAtlasTexture.getSprite("minecraft:blocks/%s_flow".formatted(fluidName));

        return sprites;
    }
}
