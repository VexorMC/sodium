package net.caffeinemc.mods.sodium.client.model.color;

import dev.vexor.radium.compat.mojang.minecraft.IBlockColor;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.blender.BlendedColorProvider;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.biome.BiomeColorSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

import java.util.Arrays;

public class DefaultColorProviders {
    public static ColorProvider<BlockState> adapt(IBlockColor color) {
        return new VanillaAdapter(color);
    }

    public static final GrassColorProvider GRASS = new GrassColorProvider();
    public static final FoliageColorProvider FOLIAGE = new FoliageColorProvider();

    public static class GrassColorProvider extends BlendedColorProvider<BlockState> {
        @Override
        protected int getColor(LevelSlice slice, BlockState state, BlockPos pos) {
            return ColorARGB.toABGR(slice.getColor(BiomeColorSource.GRASS, pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    public static class FoliageColorProvider extends BlendedColorProvider<BlockState> {
        @Override
        protected int getColor(LevelSlice slice, BlockState state, BlockPos pos) {
            return ColorARGB.toABGR(slice.getColor(BiomeColorSource.FOLIAGE, pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    private static class VanillaAdapter implements ColorProvider<BlockState> {
        private final IBlockColor color;

        private VanillaAdapter(IBlockColor color) {
            this.color = color;
        }

        @Override
        public void getColors(LevelSlice slice, BlockPos pos, BlockState state, ModelQuadView quad, int[] output) {
            Arrays.fill(output, ColorARGB.toABGR(this.color.colorMultiplier(state, slice, pos, quad.getTintIndex())));
        }
    }
}
