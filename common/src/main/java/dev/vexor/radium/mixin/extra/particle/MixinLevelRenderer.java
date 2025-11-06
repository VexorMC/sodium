package dev.vexor.radium.mixin.extra.particle;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import dev.vexor.radium.extra.client.gui.SodiumExtraGameOptions;
import net.minecraft.client.particle.ParticleType;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public class MixinLevelRenderer {
    @Unique
    private static final SodiumExtraGameOptions.ParticleSettings settings = SodiumExtraClientMod.options().particleSettings;

    @Redirect(method = "tickRainSplashing", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/client/particle/ParticleType;DDDDDD[I)V", ordinal = 1))
    public void tickRainSplashing(ClientWorld instance, ParticleType type, double x, double y, double z, double velocityX, double velocityY, double velocityZ, int... args) {
        if (settings.particles && settings.rainSplash) {
            instance.addParticle(type, x, y, z, velocityX, velocityY, velocityZ, args);
        }
    }

    @Inject(method = "renderWeather", at = @At(value = "HEAD"), cancellable = true, locals = LocalCapture.NO_CAPTURE)
    private void renderWeather(CallbackInfo ci) {
        if (!(SodiumExtraClientMod.options().detailSettings.rainSnow)) {
            ci.cancel();
        }
    }
}
