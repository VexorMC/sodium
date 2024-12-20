package net.caffeinemc.mods.sodium.client.model.color;

import dev.vexor.radium.compat.mojang.minecraft.BlockColors;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.caffeinemc.mods.sodium.client.model.color.interop.BlockColorsExtension;
import net.caffeinemc.mods.sodium.client.services.FluidRendererFactory;
import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;


// TODO: Make the registry a global somewhere that is only initialized once after content load
public class ColorProviderRegistry {
    private final Reference2ReferenceMap<Block, ColorProvider<BlockState>> blocks = new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceMap<Block, ColorProvider<BlockState>> fluids = new Reference2ReferenceOpenHashMap<>();

    private final ReferenceSet<Block> overridenBlocks;

    public ColorProviderRegistry(BlockColors blockColors) {
        var providers = BlockColorsExtension.getProviders(blockColors);

        for (var entry : providers.reference2ReferenceEntrySet()) {
            this.blocks.put(entry.getKey(), DefaultColorProviders.adapt(entry.getValue()));
        }

        this.overridenBlocks = BlockColorsExtension.getOverridenVanillaBlocks(blockColors);

        this.installOverrides();
    }

    // TODO: Allow mods to install their own color resolvers here
    private void installOverrides() {
        this.registerBlocks(DefaultColorProviders.GrassColorProvider.BLOCKS,
                Blocks.GRASS, Blocks.SUGARCANE, Blocks.TALLGRASS);

        this.registerBlocks(DefaultColorProviders.FoliageColorProvider.BLOCKS,
                Blocks.LEAVES, Blocks.LEAVES2, Blocks.VINE);

        this.registerBlocks(FluidRendererFactory.getInstance().getWaterBlockColorProvider(),
                Blocks.WATER);

        this.registerFluids(FluidRendererFactory.getInstance().getWaterColorProvider(),
                Blocks.WATER, Blocks.FLOWING_WATER);
    }

    private void registerBlocks(ColorProvider<BlockState> provider, Block... blocks) {
        for (var block : blocks) {
            // Do not register our resolver if the block is overriden
            if (this.overridenBlocks.contains(block))
                continue;
            this.blocks.put(block, provider);
        }
    }

    private void registerFluids(ColorProvider<BlockState> provider, AbstractFluidBlock... fluids) {
        for (var fluid : fluids) {
            this.fluids.put(fluid, provider);
        }
    }

    public ColorProvider<BlockState> getColorProvider(Block block) {
        return this.blocks.get(block);
    }

    public ColorProvider<BlockState> getColorProvider(AbstractFluidBlock fluid) {
        return this.fluids.get(fluid);
    }
}
