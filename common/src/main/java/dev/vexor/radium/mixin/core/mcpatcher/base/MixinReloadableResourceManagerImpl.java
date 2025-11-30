package dev.vexor.radium.mixin.core.mcpatcher.base;

import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.prupe.mcpatcher.mal.resource.TexturePackChangeHandler;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class MixinReloadableResourceManagerImpl {

    @Inject(method = "notifyListeners()V", at = @At("HEAD"))
    private void modifyNotifyReloadListeners1(CallbackInfo ci) {
        TexturePackChangeHandler.beforeChange1();
    }

    @Inject(method = "notifyListeners()V", at = @At("RETURN"))
    private void modifyNotifyReloadListeners2(CallbackInfo ci) {
        TexturePackChangeHandler.afterChange1();
    }
}