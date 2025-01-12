package dev.vexor.radium.mixin.extra.steady_debug_hud;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(DebugHud.class)
public abstract class MixinDebugScreenOverlay {
    @Unique
    private final List<String> leftTextCache = new ArrayList<>();
    @Unique
    private final List<String> rightTextCache = new ArrayList<>();
    @Unique
    private long nextTime = 0L;
    @Unique
    private boolean rebuild = true;

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void preRender(CallbackInfo ci) {
        if (SodiumExtraClientMod.options().extraSettings.steadyDebugHud) {
            final long currentTime = System.currentTimeMillis();
            if (currentTime > this.nextTime) {
                this.rebuild = true;
                this.nextTime = currentTime + (SodiumExtraClientMod.options().extraSettings.steadyDebugHudRefreshInterval * 50L);
            } else {
                this.rebuild = false;
            }
        } else {
            this.rebuild = true;
        }
    }

    @Redirect(method = "renderLeftText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getLeftText()Ljava/util/List;"))
    public List<String> sodiumExtra$redirectRenderLeftText(DebugHud debugHud) {
        return this.leftTextCache;
    }
    @Redirect(method = "renderRightText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getRightText()Ljava/util/List;"))
    public List<String> sodiumExtra$redirectRenderRightText(DebugHud debugHud) {
        return this.rightTextCache;
    }

    @Inject(method = "getLeftText", at = @At(value = "RETURN"))
    public void sodiumExtra$redirectDrawLeftText(CallbackInfoReturnable<List<String>> cir) {
        if (this.rebuild) {
            this.leftTextCache.clear();
            this.leftTextCache.addAll(cir.getReturnValue());
        }
    }

    @Inject(method = "getRightText", at = @At(value = "RETURN"))
    public void sodiumExtra$redirectDrawRightText(CallbackInfoReturnable<List<String>> cir) {
        if (this.rebuild) {
            this.rightTextCache.clear();
            this.rightTextCache.addAll(cir.getReturnValue());
        }
    }
}
