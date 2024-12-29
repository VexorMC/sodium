package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.fluid;

import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class FabricColorProvider {
    public static ColorProvider adapt() {
        return new FabricFluidAdapter();
    }

    private static class FabricFluidAdapter implements ColorProvider {
        public FabricFluidAdapter() {
        }

        @Override
        public void getColors(LevelSlice slice, BlockPos pos, ModelQuadView quad, int[] output) {
            Arrays.fill(output, 0xFF000000 | BiomeColors.getWaterColor(slice, pos));
        }
    }
}
