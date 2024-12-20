package net.caffeinemc.mods.sodium.mixin.core.world.map;

import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkStatus;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTrackerHolder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.SaveHandler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;


@Mixin(ClientWorld.class)
public abstract class ClientLevelMixin extends World implements ChunkTrackerHolder {
    @Unique
    private final ChunkTracker chunkTracker = new ChunkTracker();

    protected ClientLevelMixin(SaveHandler saveHandler, LevelProperties levelProperties, Dimension dimension, Profiler profiler, boolean bl) {
        super(saveHandler, levelProperties, dimension, profiler, bl);
    }

    @Override
    public ChunkTracker sodium$getTracker() {
        return Objects.requireNonNull(this.chunkTracker);
    }

    @Inject(method = "handleChunk", at = @At("HEAD"))
    private void sodium$trackChunkUnload(int x, int z, boolean load, CallbackInfo ci) {
        if (!load) {
            this.chunkTracker.onChunkStatusRemoved(x, z, ChunkStatus.FLAG_ALL);
        }
    }

    @Override
    public void onLightUpdate(BlockPos pos) {
        super.onLightUpdate(pos);

    }
}
