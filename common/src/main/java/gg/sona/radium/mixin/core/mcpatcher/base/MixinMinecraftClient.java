package gg.sona.radium.mixin.core.mcpatcher.base;

import gg.sona.radium.mcpatcher.MCPatcherUtils;
import gg.sona.radium.mcpatcher.mal.resource.TexturePackChangeHandler;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Inject(
            method = "<init>",
            at = @At("RETURN"))
    private void modifyConstructor(RunArgs runArgs, CallbackInfo ci) {
        MCPatcherUtils.setMinecraft(runArgs.directories.runDir);
    }

    @Inject(
            method = "initializeGame",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resource/ReloadableResourceManager;registerListener(Lnet/minecraft/resource/ResourceReloadListener;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER))
    private void modifyStartGame2(CallbackInfo ci) {
        TexturePackChangeHandler.beforeChange1();
        ConfigManager.registerConfigsLate();
    }

    @Inject(
            method = "initializeGame",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;viewport(IIII)V",
                    shift = At.Shift.AFTER))
    private void modifyStartGame3(CallbackInfo ci) {
        TexturePackChangeHandler.afterChange1();
    }

    @Inject(method = "runGameLoop()V", at = @At(value = "HEAD"))
    private void modifyRunGameLoop(CallbackInfo ci) {
        TexturePackChangeHandler.checkForTexturePackChange();
    }
}
