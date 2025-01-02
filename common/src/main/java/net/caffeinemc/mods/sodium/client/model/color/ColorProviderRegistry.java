package net.caffeinemc.mods.sodium.client.model.color;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class ColorProviderRegistry {
    private final Reference2ReferenceMap<Block, ColorProvider> blocks = new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceMap<Block, ColorProvider> fluids = new Reference2ReferenceOpenHashMap<>();

    public ColorProviderRegistry() {
        this.installOverrides();
        this.blocks.defaultReturnValue(DefaultColorProviders.BLOCK);
    }

    // TODO: Allow mods to install their own color resolvers here
    private void installOverrides() {
        this.registerBlocks(DefaultColorProviders.GRASS, Blocks.GRASS, Blocks.SUGARCANE, Blocks.TALLGRASS, Blocks.DOUBLE_PLANT);
        this.registerBlocks(DefaultColorProviders.FOLIAGE, Blocks.LEAVES, Blocks.LEAVES2, Blocks.VINE);
        this.registerBlocks(DefaultColorProviders.WATER, Blocks.WATER);
        this.registerFluids(DefaultColorProviders.WATER, Blocks.WATER, Blocks.FLOWING_WATER);
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
