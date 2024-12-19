package net.caffeinemc.mods.sodium.client.render.chunk.map;

import net.minecraft.client.world.ClientWorld;

public interface ChunkTrackerHolder {
    static ChunkTracker get(ClientWorld level) {
        return ((ChunkTrackerHolder) level).sodium$getTracker();
    }

    ChunkTracker sodium$getTracker();
}
