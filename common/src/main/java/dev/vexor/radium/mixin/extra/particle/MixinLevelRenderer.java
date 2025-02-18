package dev.vexor.radium.mixin.extra.particle;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "tickRainSplashing", at = @At(value = "HEAD"), cancellable = true)
    public void tickRainSplashing(CallbackInfo ci) {
        if (!(SodiumExtraClientMod.options().particleSettings.particles && SodiumExtraClientMod.options().particleSettings.rainSplash)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderWeather", at = @At(value = "HEAD"), cancellable = true, locals = LocalCapture.NO_CAPTURE)
    private void renderWeather(CallbackInfo ci) {
        if (!(SodiumExtraClientMod.options().detailSettings.rainSnow)) {
            ci.cancel();
        }
    }
}
