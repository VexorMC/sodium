package net.caffeinemc.mods.sodium.client.render.chunk.map;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.util.math.ChunkPos;

public class ChunkTracker implements ClientChunkEventListener {
    private final Long2IntOpenHashMap chunkStatus = new Long2IntOpenHashMap();
    private final LongOpenHashSet chunkReady = new LongOpenHashSet();

    private final LongSet unloadQueue = new LongOpenHashSet();
    private final LongSet loadQueue = new LongOpenHashSet();

    public ChunkTracker() {

    }

    @Override
    public void updateMapCenter(int chunkX, int chunkZ) {

    }

    @Override
    public void updateLoadDistance(int loadDistance) {

    }

    @Override
    public void onChunkStatusAdded(int x, int z, int flags) {
        var key = ChunkPos.getIdFromCoords(x, z);

        var prev = this.chunkStatus.get(key);
        var cur = prev | flags;

        if (prev == cur) {
            return;
        }

        this.chunkStatus.put(key, cur);

        this.updateNeighbors(x, z);
    }

    @Override
    public void onChunkStatusRemoved(int x, int z, int flags) {
        var key = ChunkPos.getIdFromCoords(x, z);

        var prev = this.chunkStatus.get(key);
        int cur = prev & ~flags;

        if (prev == cur) {
            return;
        }

        if (cur == this.chunkStatus.defaultReturnValue()) {
            this.chunkStatus.remove(key);
        } else {
            this.chunkStatus.put(key, cur);
        }

        this.updateNeighbors(x, z);
    }

    private void updateNeighbors(int x, int z) {
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                this.updateMerged(ox + x, oz + z);
            }
        }
    }

    private void updateMerged(int x, int z) {
        long key = ChunkPos.getIdFromCoords(x, z);

        int flags = this.chunkStatus.get(key);

        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                flags &= this.chunkStatus.get(ChunkPos.getIdFromCoords(ox + x, oz + z));
            }
        }

        if (flags == ChunkStatus.FLAG_ALL) {
            if (this.chunkReady.add(key) && !this.unloadQueue.remove(key)) {
                this.loadQueue.add(key);
            }
        } else {
            if (this.chunkReady.remove(key) && !this.loadQueue.remove(key)) {
                this.unloadQueue.add(key);
            }
        }
    }

    public LongCollection getReadyChunks() {
        return LongSets.unmodifiable(this.chunkReady);
    }

    public void forEachEvent(ChunkEventHandler loadEventHandler, ChunkEventHandler unloadEventHandler) {
        forEachChunk(this.unloadQueue, unloadEventHandler);
        this.unloadQueue.clear();

        forEachChunk(this.loadQueue, loadEventHandler);
        this.loadQueue.clear();
    }

    public static void forEachChunk(LongCollection queue, ChunkEventHandler handler) {
        var iterator = queue.iterator();

        while (iterator.hasNext()) {
            var pos = iterator.nextLong();

            var coords = getCoordsFromId(pos);

            var x = coords[0];
            var z = coords[1];

            handler.apply(x, z);
        }
    }

    public static int[] getCoordsFromId(long id) {
        int x = (int)(id & 4294967295L); // Extract lower 32 bits
        int z = (int)((id >> 32) & 4294967295L); // Extract upper 32 bits
        return new int[]{x, z};
    }

    public interface ChunkEventHandler {
        void apply(int x, int z);
    }
}