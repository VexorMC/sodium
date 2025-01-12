package dev.vexor.radium.mixin.extra.particle;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleEngine {
    @Inject(method = "addBlockBreakParticles", at = @At(value = "HEAD"), cancellable = true)
    public void addBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().particleSettings.particles || !SodiumExtraClientMod.options().particleSettings.blockBreak) {
            ci.cancel();
        }
    }

    @Inject(method = "addBlockBreakingParticles", at = @At(value = "HEAD"), cancellable = true)
    public void addBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().particleSettings.particles || !SodiumExtraClientMod.options().particleSettings.blockBreaking) {
            ci.cancel();
        }
    }
}
