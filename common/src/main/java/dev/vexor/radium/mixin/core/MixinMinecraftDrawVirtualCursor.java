package dev.vexor.radium.mixin.core;

import dev.vexor.radium.lwjgl3.implementation.glfw.VirtualGLFWMouseImplementation;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftDrawVirtualCursor {
    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;unbind()V"))
    private void drawVirtualCursor(CallbackInfo ci) {
        VirtualGLFWMouseImplementation.render();
    }
}
