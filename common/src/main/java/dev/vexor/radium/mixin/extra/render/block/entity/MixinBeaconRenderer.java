package dev.vexor.radium.mixin.extra.render.block.entity;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BeaconBlockEntityRenderer.class, priority = 999)
public abstract class MixinBeaconRenderer {

    @Inject(method = "render(Lnet/minecraft/block/entity/BeaconBlockEntity;DDDFI)V", at = @At(value = "HEAD"), cancellable = true)
    public void render(BeaconBlockEntity beaconBlockEntity, double d, double e, double f, float g, int i, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().renderSettings.beaconBeam)
            ci.cancel();
    }
}
