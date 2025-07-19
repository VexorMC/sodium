package dev.vexor.radium.mixin.extra.core;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinGui {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(float tickDelta, CallbackInfo ci) {
    }
}
