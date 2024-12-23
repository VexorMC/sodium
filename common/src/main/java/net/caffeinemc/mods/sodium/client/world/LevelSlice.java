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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.util.math.Vec3d;
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

    // (Local Section -> Light Manager) table.
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private final SodiumAuxiliaryLightManager[] auxLightManager;

    // (Local Section -> Light Arrays) table.
    private final @Nullable ChunkNibbleArray[][] lightArrays;

    // (Local Section -> Block Entity) table.
    private final @Nullable Int2ReferenceMap<BlockEntity>[] blockEntityArrays;

    // The starting point from which this slice captures blocks
    private int originBlockX, originBlockY, originBlockZ;

    // The volume that this WorldSlice contains
    private Box volume;

    public static ChunkRenderContext prepare(World level, SectionPos pos, ClonedChunkSectionCache cache) {
        Chunk chunk = level.getChunk(pos.getX(), pos.getZ());
        ChunkSection section = chunk.getBlockStorage()[pos.getY()];

        // If the chunk section is absent or empty, simply terminate now. There will never be anything in this chunk
        // section to render, so we need to signal that a chunk render task shouldn't be created. This saves a considerable
        // amount of time in queueing instant build tasks and greatly accelerates how quickly the level can be loaded.
        if (section == null || section.isEmpty()) {
            return null;
        }

        Box box = new Box(pos.minBlockX() - NEIGHBOR_BLOCK_RADIUS,
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

        List<?> renderers = PlatformLevelRenderHooks.getInstance().retrieveChunkMeshAppenders(level, pos.origin());

        return new ChunkRenderContext(pos, sections, box, renderers);
    }

    @SuppressWarnings("unchecked")
    public LevelSlice(ClientWorld level) {
        this.level = level;

        this.blockArrays = new BlockState[SECTION_ARRAY_SIZE][SECTION_BLOCK_COUNT];
        this.lightArrays = new ChunkNibbleArray[SECTION_ARRAY_SIZE][LIGHT_TYPES.length];

        this.blockEntityArrays = new Int2ReferenceMap[SECTION_ARRAY_SIZE];
        this.auxLightManager = new SodiumAuxiliaryLightManager[SECTION_ARRAY_SIZE];

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
        this.auxLightManager[sectionIndex] = section.getAuxLightManager();
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

            int minBlockX = (int) Math.max(bounds.minX, sectionPos.minBlockX());
            int maxBlockX = (int) Math.min(bounds.maxX, sectionPos.maxBlockX());

            int minBlockY = (int) Math.max(bounds.minY, sectionPos.minBlockY());
            int maxBlockY = (int) Math.min(bounds.maxY, sectionPos.maxBlockY());

            int minBlockZ = (int) Math.max(bounds.minZ, sectionPos.minBlockZ());
            int maxBlockZ = (int) Math.min(bounds.maxZ, sectionPos.maxBlockZ());

            for (int x = minBlockX; x <= maxBlockX; x++) {
                for (int y = minBlockY; y <= maxBlockY; y++) {
                    for (int z = minBlockZ; z <= maxBlockZ; z++) {
                        int index = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15);
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
            this.auxLightManager[sectionIndex] = null;
        }
    }

    @Override
    public @NotNull BlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
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

    public BlockState getBlockState(int blockX, int blockY, int blockZ) {
        if (!this.volume.contains(new Vec3d(blockX, blockY, blockZ))) {
            return EMPTY_BLOCK_STATE;
        }

        int relBlockX = blockX - this.originBlockX;
        int relBlockY = blockY - this.originBlockY;
        int relBlockZ = blockZ - this.originBlockZ;

        return this.blockArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)]
                [getLocalBlockIndex(relBlockX & 15, relBlockY & 15, relBlockZ & 15)];
    }

    public int getSkyLight(BlockPos pos) {
        if (!this.volume.contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) {
            return 0;
        }
        int relBlockX = pos.getX() - this.originBlockX;
        int relBlockY = pos.getY() - this.originBlockY;
        int relBlockZ = pos.getZ() - this.originBlockZ;

        var lightArrays = this.lightArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];

        var skyLightArray = lightArrays[LightType.SKY.ordinal()];

        int localBlockX = relBlockX & 15;
        int localBlockY = relBlockY & 15;
        int localBlockZ = relBlockZ & 15;

        return skyLightArray == null ? 0 : skyLightArray.get(localBlockX, localBlockY, localBlockZ);
    }

    public int getBlockLight(BlockPos pos) {
        if (!this.volume.contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) {
            return 0;
        }
        int relBlockX = pos.getX() - this.originBlockX;
        int relBlockY = pos.getY() - this.originBlockY;
        int relBlockZ = pos.getZ() - this.originBlockZ;

        var lightArrays = this.lightArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];

        var blockLightArray = lightArrays[LightType.BLOCK.ordinal()];

        int localBlockX = relBlockX & 15;
        int localBlockY = relBlockY & 15;
        int localBlockZ = relBlockZ & 15;

        return blockLightArray == null ? 0 : blockLightArray.get(localBlockX, localBlockY, localBlockZ);
    }

    @Override
    public int getLight(BlockPos pos, int ambientDarkness) {
        if (!this.volume.contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) {
            return 0;
        }

        int relBlockX = pos.getX() - this.originBlockX;
        int relBlockY = pos.getY() - this.originBlockY;
        int relBlockZ = pos.getZ() - this.originBlockZ;

        var lightArrays = this.lightArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];

        var skyLightArray = lightArrays[LightType.SKY.ordinal()];
        var blockLightArray = lightArrays[LightType.BLOCK.ordinal()];

        int localBlockX = relBlockX & 15;
        int localBlockY = relBlockY & 15;
        int localBlockZ = relBlockZ & 15;

        int skyLight = skyLightArray == null ? 0 : skyLightArray.get(localBlockX, localBlockY, localBlockZ) - ambientDarkness;
        int blockLight = blockLightArray == null ? 0 : blockLightArray.get(localBlockX, localBlockY, localBlockZ);

        return Math.max(blockLight, skyLight);
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.getBlockEntity(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockEntity getBlockEntity(int blockX, int blockY, int blockZ) {
        if (!this.volume.contains(new Vec3d(blockX, blockY, blockZ))) {
            return null;
        }

        int relBlockX = blockX - this.originBlockX;
        int relBlockY = blockY - this.originBlockY;
        int relBlockZ = blockZ - this.originBlockZ;

        var blockEntities = this.blockEntityArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];

        if (blockEntities == null) {
            return null;
        }

        return blockEntities.get(getLocalBlockIndex(relBlockX & 15, relBlockY & 15, relBlockZ & 15));
    }

    public static int getLocalBlockIndex(int blockX, int blockY, int blockZ) {
        return (blockY << LOCAL_XYZ_BITS << LOCAL_XYZ_BITS) | (blockZ << LOCAL_XYZ_BITS) | blockX;
    }

    public static int getLocalSectionIndex(int sectionX, int sectionY, int sectionZ) {
        return (sectionY * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH) + (sectionZ * SECTION_ARRAY_LENGTH) + sectionX;
    }

    public float getBrightness(Direction direction, boolean shaded) {
        if (!shaded) {
            return this.level.dimension.hasNoSkylight() ? 0.9f : 1.0f;
        }
        return switch (direction) {
            case DOWN -> .5f;
            case UP -> 1f;
            case NORTH, SOUTH -> .8f;
            default -> .6f;
        };
    }

    public int getColor(BiomeColorSource source, int blockX, int blockY, int blockZ) {
        return this.biomeColors.getColor(source.getProvider(), blockX, blockY, blockZ);
    }
}
