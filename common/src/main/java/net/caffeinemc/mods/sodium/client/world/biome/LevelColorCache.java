package net.caffeinemc.mods.sodium.client.world.biome;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.util.color.BoxBlur;
import net.caffeinemc.mods.sodium.client.util.color.BoxBlur.ColorBuffer;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

public class LevelColorCache {
    private static final int NEIGHBOR_BLOCK_RADIUS = 2;
    private final LevelBiomeSlice biomeData;

    private final Reference2ReferenceOpenHashMap<BiomeColors.ColorProvider, Slice[]> slices;
    private long populateStamp;

    private final int blendRadius;

    private final ColorBuffer tempColorBuffer;

    private int minBlockX, minBlockY, minBlockZ;
    private int maxBlockX, maxBlockY, maxBlockZ;

    private final int sizeXZ, sizeY;

    public LevelColorCache(LevelBiomeSlice biomeData, int blendRadius) {
        this.biomeData = biomeData;
        this.blendRadius = blendRadius;

        this.sizeXZ = 16 + ((NEIGHBOR_BLOCK_RADIUS + this.blendRadius) * 2);
        this.sizeY = 16 + (NEIGHBOR_BLOCK_RADIUS * 2);

        this.slices = new Reference2ReferenceOpenHashMap<>();
        this.populateStamp = 1;

        this.tempColorBuffer = new ColorBuffer(this.sizeXZ, this.sizeXZ);
    }

    public void update(ChunkRenderContext context) {
        this.minBlockX = (context.origin().minBlockX() - NEIGHBOR_BLOCK_RADIUS);
        this.minBlockY = (context.origin().minBlockY() - NEIGHBOR_BLOCK_RADIUS);
        this.minBlockZ = (context.origin().minBlockZ() - NEIGHBOR_BLOCK_RADIUS);

        this.maxBlockX = (context.origin().maxBlockX() + NEIGHBOR_BLOCK_RADIUS);
        this.maxBlockY = (context.origin().maxBlockY() + NEIGHBOR_BLOCK_RADIUS);
        this.maxBlockZ = (context.origin().maxBlockZ() + NEIGHBOR_BLOCK_RADIUS);

        this.populateStamp++;
    }

    public int getColor(BiomeColors.ColorProvider resolver, int blockX, int blockY, int blockZ) {
        // Clamp inputs
        blockX = MathHelper.clamp(blockX, this.minBlockX, this.maxBlockX) - this.minBlockX;
        blockY = MathHelper.clamp(blockY, this.minBlockY, this.maxBlockY) - this.minBlockY;
        blockZ = MathHelper.clamp(blockZ, this.minBlockZ, this.maxBlockZ) - this.minBlockZ;

        if (!this.slices.containsKey(resolver)) {
            this.initializeSlices(resolver);
        }

        var slice = this.slices.get(resolver)[blockY];

        if (slice.lastPopulateStamp < this.populateStamp) {
            this.updateColorBuffers(blockY, resolver, slice);
        }

        var buffer = slice.getBuffer();

        return buffer.get(blockX + this.blendRadius, blockZ + this.blendRadius);
    }

    private void initializeSlices(BiomeColors.ColorProvider resolver) {
        var slice = new Slice[this.sizeY];

        for (int blockY = 0; blockY < this.sizeY; blockY++) {
            slice[blockY] = new Slice(this.sizeXZ);
        }

        this.slices.put(resolver, slice);
    }

    private void updateColorBuffers(int relY, BiomeColors.ColorProvider resolver, Slice slice) {
        int blockY = this.minBlockY + relY;

        int minBlockZ = this.minBlockZ - this.blendRadius;
        int minBlockX = this.minBlockX - this.blendRadius;

        int maxBlockZ = this.maxBlockZ + this.blendRadius;
        int maxBlockX = this.maxBlockX + this.blendRadius;

        ColorBuffer buffer = slice.buffer;

        for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ++) {
            for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
                Biome biome = this.biomeData.getBiome(blockX, blockY, blockZ);

                int relBlockX = blockX - minBlockX;
                int relBlockZ = blockZ - minBlockZ;

                buffer.set(relBlockX, relBlockZ, resolver.getColorAtPos(biome, new BlockPos(blockX, blockY, blockZ)));
            }
        }

        if (this.blendRadius > 0) {
            BoxBlur.blur(buffer.data, this.tempColorBuffer.data, this.sizeXZ, this.sizeXZ, this.blendRadius);
        }

        slice.lastPopulateStamp = this.populateStamp;
    }

    private static class Slice {
        private final ColorBuffer buffer;
        private long lastPopulateStamp;

        private Slice(int size) {
            this.buffer = new ColorBuffer(size, size);
            this.lastPopulateStamp = 0;
        }

        public ColorBuffer getBuffer() {
            return this.buffer;
        }
    }
}
