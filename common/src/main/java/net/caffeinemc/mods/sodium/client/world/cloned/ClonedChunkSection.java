package net.caffeinemc.mods.sodium.client.world.cloned;

import dev.vexor.radium.compat.mojang.minecraft.ChunkNibbleArrayExt;
import gg.sona.radium.mixin.sodium.core.access.AChunk;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClonedChunkSection {
    private static final ChunkNibbleArray DEFAULT_SKY_LIGHT_ARRAY = new ChunkNibbleArrayExt(15);
    private static final ChunkNibbleArray DEFAULT_BLOCK_LIGHT_ARRAY = new ChunkNibbleArray();
    private static final BlockState EMPTY_BLOCK_STATE = Blocks.AIR.getDefaultState();

    private final SectionPos pos;
    private final @Nullable Int2ReferenceMap<BlockEntity> blockEntityMap;
    private final @Nullable ChunkNibbleArray[] lightDataArrays;
    private final @Nullable BlockState[] blockData;
    private final @Nullable Biome[] biomeData;

    private long lastUsedTimestamp = Long.MAX_VALUE;

    public ClonedChunkSection(World level, Chunk chunk, @Nullable ChunkSection section, SectionPos pos) {
        this.pos = pos;

        Biome[] biomeData = null;
        BlockState[] blockData = null;
        Int2ReferenceMap<BlockEntity> blockEntityMap = null;

        if (section != null) {
            if (!section.isEmpty()) {
                var blockStates = section.getBlockStates();
                if (blockStates != null) {
                    blockData = new BlockState[blockStates.length];

                    for (int i = 0; i < blockStates.length; i++) {
                        var state = Block.BLOCK_STATES.fromId(blockStates[i]);
                        blockData[i] = state == null ? EMPTY_BLOCK_STATE : state;
                    }
                }

                blockEntityMap = copyBlockEntities(chunk, pos);
            }

            biomeData = convertBiomeArray(chunk.getBiomeArray());
        }

        this.biomeData = biomeData;
        this.blockData = blockData;
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
        if (section != null) {
            return switch (type) {
                case SKY -> section.getSkyLight();
                case BLOCK -> section.getBlockLight();
            };
        }

        return switch (type) {
            case SKY -> DEFAULT_SKY_LIGHT_ARRAY;
            case BLOCK -> DEFAULT_BLOCK_LIGHT_ARRAY;
        };
    }

    private static @NotNull Int2ReferenceMap<BlockEntity> copyBlockEntities(Chunk chunk, SectionPos pos) {
        Int2ReferenceOpenHashMap<BlockEntity> blockEntities = new Int2ReferenceOpenHashMap<>();

        for (int y = pos.minBlockY(); y <= pos.maxBlockY(); y++) {
            for (int z = pos.minBlockZ(); z <= pos.maxBlockZ(); z++) {
                for (int x = pos.minBlockX(); x <= pos.maxBlockX(); x++) {
                    Block block = ((AChunk) chunk).invokeGetBlock(x & 15, y, z & 15);
                    if (!block.hasBlockEntity()) {
                        continue;
                    }

                    BlockEntity blockEntity = chunk.getBlockEntity(new BlockPos(x, y, z), Chunk.Status.IMMEDIATE);
                    if (blockEntity != null) {
                        blockEntities.put(LevelSlice.getLocalBlockIndex(x & 15, y & 15, z & 15), blockEntity);
                    }
                }
            }
        }

        return blockEntities;
    }

    public SectionPos getPosition() {
        return this.pos;
    }

    public @Nullable BlockState[] getBlockData() {
        return this.blockData;
    }

    public @Nullable Biome[] getBiomeData() {
        return this.biomeData;
    }

    public @Nullable Int2ReferenceMap<BlockEntity> getBlockEntityMap() {
        return this.blockEntityMap;
    }

    public @Nullable ChunkNibbleArray getLightArray(LightType type) {
        return this.lightDataArrays[type.ordinal()];
    }

    public long getLastUsedTimestamp() {
        return this.lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long timestamp) {
        this.lastUsedTimestamp = timestamp;
    }
}
