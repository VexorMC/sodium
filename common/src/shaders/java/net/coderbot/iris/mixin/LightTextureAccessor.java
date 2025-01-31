package net.coderbot.iris.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface LightTextureAccessor {
    @Accessor("lightmapTexture")
    NativeImageBackedTexture getLightTexture();
}