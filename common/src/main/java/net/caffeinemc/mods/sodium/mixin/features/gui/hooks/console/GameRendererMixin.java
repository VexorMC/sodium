package net.caffeinemc.mods.sodium.mixin.features.gui.hooks.console;

import net.caffeinemc.mods.sodium.client.gui.console.ConsoleHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V", shift = At.Shift.AFTER))
    private void onRender(float tickDelta, long nanoTime, CallbackInfo ci) {
        client.profiler.push("sodium_console_overlay");

        ConsoleHooks.render(Sys.getTime());

        client.profiler.pop();
    }
}
