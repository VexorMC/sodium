package dev.vexor.radium.mixin.extra.render.block.entity;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntityRenderer.class)
public class MixinPistonBlockEntityRenderer {
    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/block/entity/PistonBlockEntity;DDDFI)V", cancellable = true)
    public void render(PistonBlockEntity pistonBlockEntity, double d, double e, double f, float g, int i, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().renderSettings.piston)
            ci.cancel();
    }
}
