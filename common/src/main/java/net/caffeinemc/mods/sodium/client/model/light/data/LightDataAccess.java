package net.caffeinemc.mods.sodium.client.model.light.data;

import dev.vexor.radium.compat.mojang.minecraft.render.LightTexture;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * The light data cache is used to make accessing the light data and occlusion properties of blocks cheaper. The data
 * for each block is stored as an integer with packed fields in order to work around the lack of value types in Java.
 *
 * This code is not very pretty, but it does perform significantly faster than the vanilla implementation and has
 * good cache locality.
 *
 * Each integer contains the following fields:
 * - BL: World block light, encoded as a 4-bit unsigned integer
 * - SL: World sky light, encoded as a 4-bit unsigned integer
 * - LU: Block luminance, encoded as a 4-bit unsigned integer
 * - AO: Ambient occlusion, floating point value in the range of 0.0..1.0 encoded as a 16-bit unsigned integer with 12-bit precision
 * - EM: Emissive test, true if block uses emissive lighting
 * - OP: Block opacity test, true if opaque
 * - FO: Full cube opacity test, true if opaque full cube
 * - FC: Full cube test, true if full cube
 *
 * You can use the various static pack/unpack methods to extract these values in a usable format.
 */
public abstract class LightDataAccess {
    private final BlockPos.Mutable pos = new BlockPos.Mutable();
    protected LevelSlice level;

    public int get(int x, int y, int z, Direction d1, Direction d2) {
        return this.get(x + d1.getOffsetX() + d2.getOffsetX(),
                y + d1.getOffsetY() + d2.getOffsetY(),
                z + d1.getOffsetZ() + d2.getOffsetZ());
    }

    public int get(int x, int y, int z, Direction dir) {
        return this.get(x + dir.getOffsetX(),
                y + dir.getOffsetY(),
                z + dir.getOffsetZ());
    }

    public int get(BlockPos pos, Direction dir) {
        return this.get(pos.getX(), pos.getY(), pos.getZ(), dir);
    }

    public int get(BlockPos pos) {
        return this.get(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Returns the light data for the block at the given position. The property fields can then be accessed using
     * the various unpack methods below.
     */
    public abstract int get(int x, int y, int z);

    protected int compute(int x, int y, int z) {
        BlockPos pos = this.pos.setPosition(x, y, z);
        LevelSlice level = this.level;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        float ao;
        boolean em;

        if (block.getLightLevel() == 0) {
            ao = block.getAmbientOcclusionLightLevel();
            em = false;
        } else {
            ao = 1.0f;
            em = true;
        }

        boolean op = !block.hasTransparency() || block.getOpacity() == 0;
        boolean fo = block.hasTransparency();
        boolean fc = block.isFullCube();

        int lu = state.getBlock().getLightLevel();

        // OPTIMIZE: Do not calculate light data if the block is full and opaque and does not emit light.
        int bl;
        int sl;
        if (fo && lu == 0) {
            bl = 0;
            sl = 0;
        } else {
            if (em) {
                bl = level.getBlockLight(pos);
                sl = level.getSkyLight(pos);
            } else {
                int light = getLightColor(state, pos);
                bl = LightTexture.block(light);
                sl = LightTexture.sky(light);
            }
        }


        return packFC(fc) | packFO(fo) | packOP(op) | packEM(em) | packAO(ao) | packLU(lu) | packSL(sl) | packBL(bl);
    }

    public int getLightColor(BlockState blockState, BlockPos blockPos) {
        boolean em;

        if (blockState.getBlock().getLightLevel() == 0) {
            em = false;
        } else {
            em = true;
        }

        int n;
        if (em) {
            return 0xF000F0;
        }
        int n2 = level.getSkyLight(blockPos);
        int n3 = level.getBlockLight(blockPos);
        if (n3 < (n = blockState.getBlock().getLightLevel())) {
            n3 = n;
        }
        return n2 << 20 | n3 << 4;
    }

    public static int packBL(int blockLight) {
        return blockLight & 0xF;
    }

    public static int unpackBL(int word) {
        return word & 0xF;
    }

    public static int packSL(int skyLight) {
        return (skyLight & 0xF) << 4;
    }

    public static int unpackSL(int word) {
        return (word >>> 4) & 0xF;
    }

    public static int packLU(int luminance) {
        return (luminance & 0xF) << 8;
    }

    public static int unpackLU(int word) {
        return (word >>> 8) & 0xF;
    }

    public static int packAO(float ao) {
        int aoi = (int) (ao * 4096.0f);
        return (aoi & 0xFFFF) << 12;
    }

    public static float unpackAO(int word) {
        int aoi = (word >>> 12) & 0xFFFF;
        return aoi * (1.0f / 4096.0f);
    }

    public static int packEM(boolean emissive) {
        return (emissive ? 1 : 0) << 28;
    }

    public static boolean unpackEM(int word) {
        return ((word >>> 28) & 0b1) != 0;
    }

    public static int packOP(boolean opaque) {
        return (opaque ? 1 : 0) << 29;
    }

    public static boolean unpackOP(int word) {
        return ((word >>> 29) & 0b1) != 0;
    }

    public static int packFO(boolean opaque) {
        return (opaque ? 1 : 0) << 30;
    }

    public static boolean unpackFO(int word) {
        return ((word >>> 30) & 0b1) != 0;
    }

    public static int packFC(boolean fullCube) {
        return (fullCube ? 1 : 0) << 31;
    }

    public static boolean unpackFC(int word) {
        return ((word >>> 31) & 0b1) != 0;
    }

    public static int getLightmap(int word) {
        return LightTexture.pack(Math.max(unpackBL(word), unpackLU(word)), unpackSL(word));
    }

    public static int getEmissiveLightmap(int word) {
        if (unpackEM(word)) {
            return LightTexture.FULL_BRIGHT;
        } else {
            return getLightmap(word);
        }
    }

    public LevelSlice getLevel() {
        return this.level;
    }
}