package net.caffeinemc.mods.sodium.mixin.core.render;

import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteAtlasTexture.class)
public class TextureAtlasMixin {
    @Inject(method = "update", at = @At("RETURN"))
    private void sodium$deleteSpriteFinder(CallbackInfo ci) {
        SpriteFinderCache.resetSpriteFinder();
    }
}