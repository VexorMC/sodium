package net.caffeinemc.mods.sodium.mixin.core.world.map;

import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkStatus;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTrackerHolder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ClientChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkProvider.class)
public class ClientChunkCacheMixin {
    @Shadow
    private World world;

    @Inject(
            method = "unloadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;unloadFromWorld()V",
                    shift = At.Shift.AFTER
            )
    )
    private void onChunkUnloaded(int x, int z, CallbackInfo ci) {
        ChunkTrackerHolder.get((ClientWorld)this.world)
                .onChunkStatusRemoved(x, z, ChunkStatus.FLAG_ALL);
    }

    @Inject(
            method = "getOrGenerateChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;setChunkLoaded(Z)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onChunkLoaded(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        ChunkTrackerHolder.get((ClientWorld)this.world)
                .onChunkStatusAdded(x, z, ChunkStatus.FLAG_ALL);
    }
}
