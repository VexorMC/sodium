package net.caffeinemc.mods.sodium.client.model.color;

import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.blender.BlendedColorProvider;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.biome.BiomeColorSource;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class DefaultColorProviders {
    public static final BlockColorProvider BLOCK = new BlockColorProvider();
    public static final WaterColorProvider WATER = new WaterColorProvider();
    public static final GrassColorProvider GRASS = new GrassColorProvider();
    public static final FoliageColorProvider FOLIAGE = new FoliageColorProvider();

    public static class BlockColorProvider implements ColorProvider {
        @Override
        public void getColors(LevelSlice slice, BlockState state, ModelQuadView quad, int[] output, BlockPos pos) {
            var block = slice.getBlockState(pos).getBlock();
            for (int i = 0; i < output.length; i++) {
                output[i] = block.getBlockColor(slice, pos, i);
            }
        }
    }

    public static class WaterColorProvider extends BlendedColorProvider {
        @Override
        protected int getColor(LevelSlice slice, BlockPos pos) {
            return slice.getColor(BiomeColorSource.WATER, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public static class GrassColorProvider extends BlendedColorProvider {
        @Override
        protected int getColor(LevelSlice slice, BlockPos pos) {
            return slice.getColor(BiomeColorSource.GRASS, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public static class FoliageColorProvider extends BlendedColorProvider {
        @Override
        protected int getColor(LevelSlice slice, BlockPos pos) {
            return slice.getColor(BiomeColorSource.FOLIAGE, pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
