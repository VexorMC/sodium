package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class BlockOcclusionCache {
    private final BlockPos.Mutable cachedPositionObject = new BlockPos.Mutable();
    /**
     * @param selfState The state of the block in the level
     * @param view The block view for this render context
     * @param selfPos The position of the block
     * @param facing The facing direction of the side to check
     * @return True if the block side facing {@param dir} is not occluded, otherwise false
     */
    public boolean shouldDrawSide(BlockState selfState, BlockView view, BlockPos selfPos, Direction facing) {
        BlockPos.Mutable otherPos = this.cachedPositionObject;
        otherPos.setPosition(selfPos.getX() + facing.getOffsetX(), selfPos.getY() + facing.getOffsetY(), selfPos.getZ() + facing.getOffsetZ());

        // Blocks can define special behavior to control whether faces are rendered.
        // This is mostly used by transparent blocks (Leaves, Glass, etc.) to not render interior faces between blocks
        // of the same type.

        // TODO: Implement Voxel Shape occlusion from upstream.

        return selfState.getBlock().isSideInvisible(view, otherPos, facing);
    }

}