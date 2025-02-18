package dev.vexor.radium.mixin.sodium.core.world.biome;

import net.caffeinemc.mods.sodium.client.world.BiomeSeedProvider;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientLevelMixin implements BiomeSeedProvider {
    @Unique
    private long biomeZoomSeed;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void captureSeed(ClientPlayNetworkHandler clientPlayNetworkHandler, LevelInfo levelInfo, int i, Difficulty difficulty, Profiler profiler, CallbackInfo ci) {
        this.biomeZoomSeed = levelInfo.getSeed();
    }

    @Override
    public long sodium$getBiomeZoomSeed() {
        return this.biomeZoomSeed;
    }
}