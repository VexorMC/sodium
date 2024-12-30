package net.caffeinemc.mods.sodium.mixin.features.textures.animations;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteExtension;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sprite.class)
public class SpriteMixin {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void update(CallbackInfo ci) {
        if (!((SpriteExtension) this).sodium$isActive() && SodiumClientMod.options().performance.animateOnlyVisibleTextures) ci.cancel();
    }
    @Inject(method = "update", at = @At("TAIL"))
    public void update$tail(CallbackInfo ci) {
        ((SpriteExtension) this).sodium$setActive(true);
    }
}
