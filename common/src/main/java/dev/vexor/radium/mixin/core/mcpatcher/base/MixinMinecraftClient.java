package dev.vexor.radium.mixin.core.mcpatcher.base;

import com.prupe.mcpatcher.MCPatcherUtils;
import com.prupe.mcpatcher.mal.resource.TexturePackChangeHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
