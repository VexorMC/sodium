package net.caffeinemc.mods.sodium.mixin.core.access;

import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidRenderer.class)
public interface AFluidRenderer {
    @Accessor
    Sprite[] getWaterSprites();
    @Accessor
    Sprite[] getLavaSprites();
}
