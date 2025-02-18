package dev.vexor.radium.mixin.extra.render.entity;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingEntityRenderer.class)
public class MixinPaintingEntityRenderer {
    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/decoration/painting/PaintingEntity;DDDFF)V", cancellable = true)
    public void render(PaintingEntity paintingEntity, double d, double e, double f, float g, float h, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().renderSettings.painting)
            ci.cancel();
    }
}
