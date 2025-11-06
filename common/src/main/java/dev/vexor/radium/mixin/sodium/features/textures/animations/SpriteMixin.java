package dev.vexor.radium.mixin.sodium.features.textures.animations;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import dev.vexor.radium.extra.client.gui.SodiumExtraGameOptions;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sprite.class)
public class SpriteMixin {
    @Shadow
    @Final
    private String name;

    @Unique
    private static final SodiumExtraGameOptions.AnimationSettings settings = SodiumExtraClientMod.options().animationSettings;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void update(CallbackInfo ci) {
        if (!settings.itemAnimations && name.contains("item")) {
            ci.cancel();
            return;
        }

        if (!settings.blockAnimations && name.contains("block")) {
            ci.cancel();
            return;
        }

        if (!settings.lava && name.startsWith("minecraft:blocks/lava_")) {
            ci.cancel();
        }

        if (!settings.water && name.startsWith("minecraft:blocks/water_")) {
            ci.cancel();
        }

        if (!settings.fire && name.startsWith("minecraft:blocks/fire_layer_")) {
            ci.cancel();
        }

        if (!settings.portal && name.equals("minecraft:blocks/portal")) {
            ci.cancel();
        }

        // todo
//        if (!((SpriteExtension) this).sodium$isActive() && SodiumClientMod.options().performance.animateOnlyVisibleTextures)
//            ci.cancel();
    }

    @Inject(method = "update", at = @At("TAIL"))
    public void update$tail(CallbackInfo ci) {
        //((SpriteExtension) this).sodium$setActive(true);
    }
}
