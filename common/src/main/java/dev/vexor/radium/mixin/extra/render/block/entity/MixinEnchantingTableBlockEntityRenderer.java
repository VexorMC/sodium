package dev.vexor.radium.mixin.extra.render.block.entity;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantingTableBlockEntityRenderer.class)
public class MixinEnchantingTableBlockEntityRenderer {
    @Inject(method = "render(Lnet/minecraft/block/entity/EnchantingTableBlockEntity;DDDFI)V", at = @At(value = "HEAD"), cancellable = true)
    public void render(EnchantingTableBlockEntity enchantingTableBlockEntity, double d, double e, double f, float g, int i, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().renderSettings.enchantingTableBook) {
            ci.cancel();
        }
    }
}
