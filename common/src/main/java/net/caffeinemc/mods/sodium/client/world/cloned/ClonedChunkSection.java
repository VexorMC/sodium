package net.caffeinemc.mods.sodium.client.world.cloned;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.services.*;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ClonedChunkSection {
    private static final ChunkNibbleArray DEFAULT_SKY_LIGHT_ARRAY = new ChunkNibbleArray();
    private static final ChunkNibbleArray DEFAULT_BLOCK_LIGHT_ARRAY = new ChunkNibbleArray();

    private final SectionPos pos;

    private final @Nullable Int2ReferenceMap<BlockEntity> blockEntityMap;

    private final @Nullable ChunkNibbleArray[] lightDataArrays;

    private final @Nullable char[] blockData;
    private final @Nullable Biome[] biomeData;

    private long lastUsedTimestamp = Long.MAX_VALUE;

    public ClonedChunkSection(World level, Chunk chunk, @Nullable ChunkSection section, SectionPos pos) {
        this.pos = pos;

        char[] blockData = null;
        Biome[] biomeData = null;

        Int2ReferenceMap<BlockEntity> blockEntityMap = null;

        if (section != null) {
            if (!section.isEmpty()) {
                blockData = section.getBlockStates();

                blockEntityMap = copyBlockEntities(chunk, pos);
            }

            biomeData = convertBiomeArray(chunk.getBiomeArray());
        }

        this.blockData = blockData;
        this.biomeData = biomeData;

        this.blockEntityMap = blockEntityMap;

        this.lightDataArrays = copyLightData(level, section);
    }

    private static Biome[] convertBiomeArray(byte[] biomeIds) {
        Biome[] biomes = new Biome[biomeIds.length];
        for (int i = 0; i < biomeIds.length; i++) {
            // Convert the byte to an unsigned int and fetch the corresponding Biome
            biomes[i] = Biome.byId(biomeIds[i] & 0xFF);
            if (biomes[i] == null) {
                biomes[i] = Biome.PLAINS; // Default to Plains if the biome is not found
            }
        }
        return biomes;
    }

    @NotNull
    private static ChunkNibbleArray[] copyLightData(World level, ChunkSection section) {
        var arrays = new ChunkNibbleArray[2];
        arrays[LightType.BLOCK.ordinal()] = copyLightArray(section, LightType.BLOCK);

        // Dimensions without sky-light should not have a default-initialized array
        if (!level.dimension.hasNoSkylight()) {
            arrays[LightType.SKY.ordinal()] = copyLightArray(section, LightType.SKY);
        }

        return arrays;
    }

    /**
     * Copies the light data array for the given light type for this chunk, or returns a default-initialized value if
     * the light array is not loaded.
     */
    @NotNull
    private static ChunkNibbleArray copyLightArray(ChunkSection section, LightType type) {
        ChunkNibbleArray array;

        if (section != null) {
            array = switch (type) {
                case SKY -> section.getSkyLight();
                case BLOCK -> section.getBlockLight();
            };
        } else {
            array = null;
        }

        if (array == null) {
            array = switch (type) {
                case SKY -> DEFAULT_SKY_LIGHT_ARRAY;
                case BLOCK -> DEFAULT_BLOCK_LIGHT_ARRAY;
            };
        }

        return array;
    }

    @Nullable
    private static Int2ReferenceMap<BlockEntity> copyBlockEntities(Chunk chunk, SectionPos chunkCoord) {
        Box box = new Box(chunkCoord.minBlockX(), chunkCoord.minBlockY(), chunkCoord.minBlockZ(),
                chunkCoord.maxBlockX(), chunkCoord.maxBlockY(), chunkCoord.maxBlockZ());

        Int2ReferenceOpenHashMap<BlockEntity> blockEntities = null;

        // Copy the block entities from the chunk into our cloned section
        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            BlockPos pos = entry.getKey();
            BlockEntity entity = entry.getValue();

            if (box.contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) {
                if (blockEntities == null) {
                    blockEntities = new Int2ReferenceOpenHashMap<>();
                }

                blockEntities.put(LevelSlice.getLocalBlockIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), entity);
            }
        }

        if (blockEntities != null) {
            blockEntities.trim();
        }

        return blockEntities;
    }

    public SectionPos getPosition() {
        return this.pos;
    }

    public @Nullable BlockState[] getBlockData() {
        if (this.blockData == null) return null;

        BlockState[] blockData = new BlockState[4096];

        for (int i = 0; i < this.blockData.length; i++) {
            blockData[i] = Block.BLOCK_STATES.fromId(this.blockData[i]);
        }

        return blockData;
    }

    public @Nullable Biome[] getBiomeData() {
        return this.biomeData;
    }

    public @Nullable Int2ReferenceMap<BlockEntity> getBlockEntityMap() {
        return this.blockEntityMap;
    }

    public @Nullable ChunkNibbleArray getLightArray(LightType lightType) {
        return this.lightDataArrays[lightType.ordinal()];
    }

    public long getLastUsedTimestamp() {
        return this.lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long timestamp) {
        this.lastUsedTimestamp = timestamp;
    }
}
