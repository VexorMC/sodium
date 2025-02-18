package dev.vexor.radium.mixin.extra.render.entity;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
abstract class MixinLivingEntityRenderer<T extends LivingEntity> extends EntityRenderer<T> {

    protected MixinLivingEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void onRender(T livingEntity, double d, double e, double f, float g, float h, CallbackInfo ci) {
        if (livingEntity instanceof ArmorStandEntity && !SodiumExtraClientMod.options().renderSettings.armorStand) {
            ci.cancel();
            if (((ArmorStandEntity) livingEntity).shouldShowName()) {
                this.renderLabelIfPresent(livingEntity, livingEntity.getCustomName(), d, e, f, 64);
            }
        }
    }
    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "HEAD"), cancellable = true)
    private <T extends LivingEntity> void shouldShowName(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof AbstractClientPlayerEntity && !SodiumExtraClientMod.options().renderSettings.playerNameTag) {
            cir.setReturnValue(false);
        }
    }
}
