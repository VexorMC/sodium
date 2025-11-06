package dev.vexor.radium.mixin.extra.animation;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import dev.vexor.radium.extra.client.gui.SodiumExtraGameOptions;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteAtlasTexture.class)
public abstract class MixinSpriteAtlasTexture extends AbstractTexture {
    @Unique
    private static final SodiumExtraGameOptions.AnimationSettings settings = SodiumExtraClientMod.options().animationSettings;

    /**
     * Early return from update to prevent sprite iteration if no animations are enabled.
     */
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void sodiumExtra$tickAnimatedSprites(CallbackInfo ci) {
        if (!settings.itemAnimations && !settings.blockAnimations) {
            ci.cancel();
        }
    }
}
