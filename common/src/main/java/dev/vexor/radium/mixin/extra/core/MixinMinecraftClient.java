package dev.vexor.radium.mixin.extra.core;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onRunTick(CallbackInfo ci) {
        SodiumExtraClientMod.onTick(((MinecraftClient) (Object) this));
    }
}
