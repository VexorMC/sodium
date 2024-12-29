package dev.vexor.radium.compat.mojang.minecraft;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface IBlockColor {
    int colorMultiplier(BlockView worldIn, BlockPos pos, int tintIndex);
}
