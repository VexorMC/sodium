package net.caffeinemc.mods.sodium.client.model.color;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.blender.BlendedColorProvider;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class ColorProviderRegistry {
    private final Reference2ReferenceMap<Block, ColorProvider> blocks = new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceMap<Block, ColorProvider> fluids = new Reference2ReferenceOpenHashMap<>();

    class WaterColors extends BlendedColorProvider {
        @Override
        protected int getColor(LevelSlice slice, BlockPos pos) {
            return BiomeColors.getWaterColor(slice, pos) | 0xFF000000;
        }
    }

    class WaterBlockColors implements ColorProvider {
        @Override
        public void getColors(LevelSlice slice, BlockPos pos, ModelQuadView quad, int[] output) {
            Arrays.fill(output, BiomeColors.getWaterColor(slice, pos) | 0xFF000000);
        }
        //@Override
        //protected int getColor(LevelSlice slice, BlockState state, BlockPos pos) {
        //    return BiomeColors.getWaterColor(slice, pos) | 0xFF000000;
        //}
    }


    public ColorProviderRegistry() {
        this.installOverrides();
    }

    // TODO: Allow mods to install their own color resolvers here
    private void installOverrides() {
        this.registerBlocks(DefaultColorProviders.GRASS, Blocks.GRASS, Blocks.SUGARCANE, Blocks.TALLGRASS, Blocks.DOUBLE_PLANT);
        this.registerBlocks(DefaultColorProviders.FOLIAGE, Blocks.LEAVES, Blocks.LEAVES2, Blocks.VINE);
        this.registerBlocks(new WaterBlockColors(), Blocks.WATER);
        this.registerFluids(new WaterBlockColors(), Blocks.WATER, Blocks.FLOWING_WATER);
    }

    private void registerBlocks(ColorProvider provider, Block... blocks) {
        for (var block : blocks) {
            this.blocks.put(block, provider);
        }
    }

    private void registerFluids(ColorProvider provider, AbstractFluidBlock... fluids) {
        for (var fluid : fluids) {
            this.fluids.put(fluid, provider);
        }
    }

    public ColorProvider getColorProvider(Block block) {
        return this.blocks.get(block);
    }

    public ColorProvider getColorProvider(AbstractFluidBlock fluid) {
        return this.fluids.get(fluid);
    }
}
