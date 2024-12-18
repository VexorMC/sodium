package net.caffeinemc.mods.sodium.client.model.color;

import dev.lunasa.compat.mojang.minecraft.IBlockColor;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.blender.BlendedColorProvider;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

import java.util.Arrays;

public class DefaultColorProviders {
    public static ColorProvider<BlockState> adapt(IBlockColor color) {
        return new VanillaAdapter(color);
    }

    public static class GrassColorProvider<T> extends BlendedColorProvider<T> {
        public static final ColorProvider<BlockState> BLOCKS = new GrassColorProvider<>();

        private GrassColorProvider() {

        }

        @Override
        protected int getColor(LevelSlice slice, T state, BlockPos pos) {
            return 0xFF000000 | BiomeColors.getGrassColor(slice, pos);
        }
    }

    public static class FoliageColorProvider<T> extends BlendedColorProvider<T> {
        public static final ColorProvider<BlockState> BLOCKS = new FoliageColorProvider<>();

        private FoliageColorProvider() {

        }

        @Override
        protected int getColor(LevelSlice slice, T state, BlockPos pos) {
            return 0xFF000000 | BiomeColors.getFoliageColor(slice, pos);
        }
    }

    private static class VanillaAdapter implements ColorProvider<BlockState> {
        private final IBlockColor color;

        private VanillaAdapter(IBlockColor color) {
            this.color = color;
        }

        @Override
        public void getColors(LevelSlice slice, BlockPos pos, BlockPos.Mutable scratchPos, BlockState state, ModelQuadView quad, int[] output) {
            Arrays.fill(output, 0xFF000000 | this.color.colorMultiplier(state, slice, pos, quad.getTintIndex()));
        }
    }
}
