package net.caffeinemc.mods.sodium.client.world;

import dev.vexor.radium.compat.mojang.math.Mth;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.services.*;
import net.caffeinemc.mods.sodium.client.world.biome.BiomeColorSource;
import net.caffeinemc.mods.sodium.client.world.biome.LevelColorCache;
import net.caffeinemc.mods.sodium.client.world.biome.LevelBiomeSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.level.LevelGeneratorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>Takes a slice of level state (block states, biome and light data arrays) and copies the data for use in off-thread
 * operations. This allows chunk build tasks to see a consistent snapshot of chunk data at the exact moment the task was
 * created.</p>
 *
 * <p>World slices are not safe to use from multiple threads at once, but the data they contain is safe from modification
 * by the main client thread.</p>
 *
 * <p>Object pooling should be used to avoid huge allocations as this class contains many large arrays.</p>
 */
public final class LevelSlice implements BlockView {
    private static final LightType[] LIGHT_TYPES = LightType.values();

    // The number of blocks in a section.
    private static final int SECTION_BLOCK_COUNT = 16 * 16 * 16;

    // The radius of blocks around the origin chunk that should be copied.
    private static final int NEIGHBOR_BLOCK_RADIUS = 2;

    // The radius of chunks around the origin chunk that should be copied.
    private static final int NEIGHBOR_CHUNK_RADIUS = Mth.roundToward(NEIGHBOR_BLOCK_RADIUS, 16) >> 4;

    // The number of sections on each axis of this slice.
    private static final int SECTION_ARRAY_LENGTH = 1 + (NEIGHBOR_CHUNK_RADIUS * 2);

    // The size of the (Local Section -> Resource) arrays.
    private static final int SECTION_ARRAY_SIZE = SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH;

    // The number of bits needed for each local X/Y/Z coordinate.
    private static final int LOCAL_XYZ_BITS = 4;

    // The default block state used for out-of-bounds access
    private static final BlockState EMPTY_BLOCK_STATE = Blocks.AIR.getDefaultState();

    // The level this slice has copied data from
    private final ClientWorld level;

    // The accessor used for fetching biome data from the slice
    private final LevelBiomeSlice biomeSlice;

    // The biome blend cache
    private final LevelColorCache biomeColors;

    // (Local Section -> Block States) table.
    private final BlockState[][] blockArrays;

    // (Local Section -> Light Arrays) table.
    private final @Nullable ChunkNibbleArray[][] lightArrays;

    // (Local Section -> Block Entity) table.
    private final @Nullable Int2ReferenceMap<BlockEntity>[] blockEntityArrays;

    // The starting point from which this slice captures blocks
    private int originBlockX, originBlockY, originBlockZ;

    // The volume that this WorldSlice contains
    private BlockBox volume;

    private final int[] defaultLightValues;

    public static ChunkRenderContext prepare(World level, SectionPos pos, ClonedChunkSectionCache cache) {
        Chunk chunk = level.getChunk(pos.getX(), pos.getZ());
        ChunkSection section = chunk.getBlockStorage()[pos.getY()];

        // If the chunk section is absent or empty, simply terminate now. There will never be anything in this chunk
        // section to render, so we need to signal that a chunk render task shouldn't be created. This saves a considerable
        // amount of time in queueing instant build tasks and greatly accelerates how quickly the level can be loaded.
        if (section == null || section.isEmpty()) {
            return null;
        }

        BlockBox box = new BlockBox(pos.minBlockX() - NEIGHBOR_BLOCK_RADIUS,
                pos.minBlockY() - NEIGHBOR_BLOCK_RADIUS,
                pos.minBlockZ() - NEIGHBOR_BLOCK_RADIUS,
                pos.maxBlockX() + NEIGHBOR_BLOCK_RADIUS,
                pos.maxBlockY() + NEIGHBOR_BLOCK_RADIUS,
                pos.maxBlockZ() + NEIGHBOR_BLOCK_RADIUS);

        // The min/max bounds of the chunks copied by this slice
        final int minChunkX = pos.getX() - NEIGHBOR_CHUNK_RADIUS;
        final int minChunkY = pos.getY() - NEIGHBOR_CHUNK_RADIUS;
        final int minChunkZ = pos.getZ() - NEIGHBOR_CHUNK_RADIUS;

        final int maxChunkX = pos.getX() + NEIGHBOR_CHUNK_RADIUS;
        final int maxChunkY = pos.getY() + NEIGHBOR_CHUNK_RADIUS;
        final int maxChunkZ = pos.getZ() + NEIGHBOR_CHUNK_RADIUS;

        ClonedChunkSection[] sections = new ClonedChunkSection[SECTION_ARRAY_SIZE];

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
                    sections[getLocalSectionIndex(chunkX - minChunkX, chunkY - minChunkY, chunkZ - minChunkZ)] =
                            cache.acquire(chunkX, chunkY, chunkZ);
                }
            }
        }

        return new ChunkRenderContext(pos, sections, box);
    }

    @SuppressWarnings("unchecked")
    public LevelSlice(ClientWorld level) {
        this.level = level;

        defaultLightValues = new int[LIGHT_TYPES.length];
        defaultLightValues[LightType.SKY.ordinal()] = level.dimension.hasNoSkylight() ? 0 : LightType.SKY.defaultValue;
        defaultLightValues[LightType.BLOCK.ordinal()] = LightType.BLOCK.defaultValue;

        this.blockArrays = new BlockState[SECTION_ARRAY_SIZE][SECTION_BLOCK_COUNT];
        this.lightArrays = new ChunkNibbleArray[SECTION_ARRAY_SIZE][LIGHT_TYPES.length];

        this.blockEntityArrays = new Int2ReferenceMap[SECTION_ARRAY_SIZE];

        var biomeBlendRadius = SodiumClientMod.options().quality.biomeBlendRadius;

        this.biomeSlice = new LevelBiomeSlice();
        this.biomeColors = new LevelColorCache(this.biomeSlice, biomeBlendRadius);

        for (BlockState[] blockArray : this.blockArrays) {
            Arrays.fill(blockArray, EMPTY_BLOCK_STATE);
        }
    }

    public void copyData(ChunkRenderContext context) {
        this.originBlockX = SectionPos.sectionToBlockCoord(context.origin().getX() - NEIGHBOR_CHUNK_RADIUS);
        this.originBlockY = SectionPos.sectionToBlockCoord(context.origin().getY() - NEIGHBOR_CHUNK_RADIUS);
        this.originBlockZ = SectionPos.sectionToBlockCoord(context.origin().getZ() - NEIGHBOR_CHUNK_RADIUS);

        this.volume = context.volume();

        for (int x = 0; x < SECTION_ARRAY_LENGTH; x++) {
            for (int y = 0; y < SECTION_ARRAY_LENGTH; y++) {
                for (int z = 0; z < SECTION_ARRAY_LENGTH; z++) {
                    this.copySectionData(context, getLocalSectionIndex(x, y, z));
                }
            }
        }

        this.biomeSlice.update(this.level, context);
        this.biomeColors.update(context);
    }

    private void copySectionData(ChunkRenderContext context, int sectionIndex) {
        var section = context.sections()[sectionIndex];

        Objects.requireNonNull(section, "Chunk section must be non-null");

        this.unpackBlockData(this.blockArrays[sectionIndex], context, section);

        this.lightArrays[sectionIndex][LightType.BLOCK.ordinal()] = section.getLightArray(LightType.BLOCK);
        this.lightArrays[sectionIndex][LightType.SKY.ordinal()] = section.getLightArray(LightType.SKY);

        this.blockEntityArrays[sectionIndex] = section.getBlockEntityMap();
    }

    private void unpackBlockData(BlockState[] blockArray, ChunkRenderContext context, ClonedChunkSection section) {
        if (section.getBlockData() == null) {
            Arrays.fill(blockArray, EMPTY_BLOCK_STATE);
            return;
        }

        BlockState[] container = section.getBlockData();
        SectionPos sectionPos = section.getPosition();

        if (sectionPos.equals(context.origin())) {
            System.arraycopy(container, 0, blockArray, 0, container.length);
        } else {
            var bounds = context.volume();

            int minBlockX = Math.max(bounds.minX, sectionPos.minBlockX());
            int maxBlockX = Math.min(bounds.maxX, sectionPos.maxBlockX());

            int minBlockY = Math.max(bounds.minY, sectionPos.minBlockY());
            int maxBlockY = Math.min(bounds.maxY, sectionPos.maxBlockY());

            int minBlockZ = Math.max(bounds.minZ, sectionPos.minBlockZ());
            int maxBlockZ = Math.min(bounds.maxZ, sectionPos.maxBlockZ());

            for (int x = minBlockX; x <= maxBlockX; x++) {
                for (int y = minBlockY; y <= maxBlockY; y++) {
                    for (int z = minBlockZ; z <= maxBlockZ; z++) {
                        int index = getLocalBlockIndex(x & 15, y & 15, z & 15);
                        blockArray[index] = container[index];
                    }
                }
            }
        }
    }


    public void reset() {
        // erase any pointers to resources we no longer need
        // no point in cleaning the pre-allocated arrays (such as block state storage) since we hold the
        // only reference.
        for (int sectionIndex = 0; sectionIndex < SECTION_ARRAY_LENGTH; sectionIndex++) {
            Arrays.fill(this.lightArrays[sectionIndex], null);

            this.blockEntityArrays[sectionIndex] = null;
        }
    }

    @Override
    public @NotNull BlockState getBlockState(BlockPos pos) {
        if (!this.volume.contains(pos)) {
            return EMPTY_BLOCK_STATE;
        }

        int relBlockX = pos.getX() - this.originBlockX;
        int relBlockY = pos.getY() - this.originBlockY;
        int relBlockZ = pos.getZ() - this.originBlockZ;

        return this.blockArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)]
                [getLocalBlockIndex(relBlockX & 15, relBlockY & 15, relBlockZ & 15)];
    }

    @Override
    public boolean isAir(BlockPos pos) {
        return getBlockState(pos).getBlock() == Blocks.AIR;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.biomeSlice.getBiome(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getStrongRedstonePower(BlockPos pos, Direction direction) {
        BlockState blockState = this.getBlockState(pos);
        return blockState.getBlock().getStrongRedstonePower(this, pos, blockState, direction);
    }

    @Override
    public LevelGeneratorType getGeneratorType() {
        return this.level.getGeneratorType();
    }

    public int getLight(LightType type, BlockPos pos) {
        if (!this.volume.contains(pos)) {
            return 0;
        }

        int relX = pos.getX() - originBlockX;
        int relY = pos.getY() - originBlockY;
        int relZ = pos.getZ() - originBlockZ;

        BlockState state = getBlockStateRelative(relX, relY, relZ);

        if (!state.getBlock().usesNeighbourLight()) {
            return getLightFor(type, relX, relY, relZ);
        } else {
            int west = getLightFor(type, relX - 1, relY, relZ);
            int east = getLightFor(type, relX + 1, relY, relZ);
            int up = getLightFor(type, relX, relY + 1, relZ);
            int down = getLightFor(type, relX, relY - 1, relZ);
            int north = getLightFor(type, relX, relY, relZ + 1);
            int south = getLightFor(type, relX, relY, relZ - 1);

            if (east > west) {
                west = east;
            }

            if (up > west) {
                west = up;
            }

            if (down > west) {
                west = down;
            }

            if (north > west) {
                west = north;
            }

            if (south > west) {
                west = south;
            }

            return west;
        }
    }

    private int getLightFor(LightType type, int relX, int relY, int relZ) {
        int sectionIdx = getLocalSectionIndex(relX >> 4, relY >> 4, relZ >> 4);

        ChunkNibbleArray lightArray = lightArrays[sectionIdx][type.ordinal()];
        if (lightArray == null) {
            // If the array is null, it means the dimension for the current world does not support that light type
            return defaultLightValues[type.ordinal()];
        }

        return lightArray.get(relX & 15, relY & 15, relZ & 15);
    }


    public BlockState getBlockStateRelative(int x, int y, int z) {
        // NOTE: Not bounds checked. We assume ChunkRenderRebuildTask is the only function using this
        int sectionIdx = getLocalSectionIndex(x >> 4, y >> 4, z >> 4);
        int blockIdx = getLocalBlockIndex(x & 15, y & 15, z & 15);

        return blockArrays[sectionIdx][blockIdx];
    }

    @Override
    public int getLight(BlockPos pos, int ambientDarkness) {
        if (!this.volume.contains(pos)) {
            return 0;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (y < 0 || y >= 256 || x < -30_000_000 || z < -30_000_000 || x >= 30_000_000 || z >= 30_000_000) {
            return (defaultLightValues[0] << 20) | (ambientDarkness << 4);
        }

        int skyBrightness = getLight(LightType.SKY, pos);
        int blockBrightness = getLight(LightType.BLOCK, pos);

        if (blockBrightness < ambientDarkness) {
            blockBrightness = ambientDarkness;
        }

        return skyBrightness << 20 | blockBrightness << 4;
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        if (!this.volume.contains(pos)) {
            return null;
        }

        int relBlockX = pos.getX() - this.originBlockX;
        int relBlockY = pos.getY() - this.originBlockY;
        int relBlockZ = pos.getZ() - this.originBlockZ;

        var blockEntities = this.blockEntityArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];

        if (blockEntities == null) {
            return null;
        }

        return blockEntities.get(getLocalBlockIndex(relBlockX & 15, relBlockY & 15, relBlockZ & 15));
        //int relBlockX = pos.getX() - this.originBlockX;
        //int relBlockY = pos.getY() - this.originBlockY;
        //int relBlockZ = pos.getZ() - this.originBlockZ;
//
        //var section = this.sections[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];
//
        //if (section == null) {
        //    return null;
        //}
        //BlockEntity e = section.getChunk().getBlockEntity(pos, Chunk.Status.IMMEDIATE);
//
        //return e;
    }

    public static int getLocalBlockIndex(int blockX, int blockY, int blockZ) {
        return (blockY << LOCAL_XYZ_BITS << LOCAL_XYZ_BITS) | (blockZ << LOCAL_XYZ_BITS) | blockX;
    }

    public static int getLocalSectionIndex(int sectionX, int sectionY, int sectionZ) {
        return (sectionY * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH) + (sectionZ * SECTION_ARRAY_LENGTH) + sectionX;
    }

    public float getBrightness(Direction direction, boolean shaded) {
        if (!shaded) {
            return level.dimension.hasNoSkylight() ? 0.9f : 1.0f;
        }
        return switch (direction) {
            case DOWN -> .5f;
            case UP -> 1f;
            case NORTH, SOUTH -> .8f;
            default -> .7f;
        };
    }

    public int getColor(BiomeColorSource source, int blockX, int blockY, int blockZ) {
        return this.biomeColors.getColor(source.getProvider(), blockX, blockY, blockZ);
    }
}
