package dev.vexor.radium.mixin.extra.prevent_shaders;

import com.mojang.blaze3d.platform.GLX;
import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Shadow
    private ShaderEffect shader;

    @Inject(method = "areShadersSupported", at = @At("HEAD"), cancellable = true)
    private void areShadersSupported(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(GLX.shadersSupported && this.shader != null && !SodiumExtraClientMod.options().extraSettings.preventShaders);
    }
}
