package net.caffeinemc.mods.sodium.client.model.color.interop;

import dev.vexor.radium.compat.mojang.minecraft.BlockColors;
import dev.vexor.radium.compat.mojang.minecraft.IBlockColor;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.block.Block;

public interface BlockColorsExtension {
    static Reference2ReferenceMap<Block, IBlockColor> getProviders(BlockColors blockColors) {
        return blockColors.sodium$getProviders();
    }

    static ReferenceSet<Block> getOverridenVanillaBlocks(BlockColors blockColors) {
        return blockColors.sodium$getOverridenVanillaBlocks();
    }

    Reference2ReferenceMap<Block, IBlockColor> sodium$getProviders();

    ReferenceSet<Block> sodium$getOverridenVanillaBlocks();
}
