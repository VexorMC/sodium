package net.caffeinemc.mods.sodium.client.services;

import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AmbientOcclusionMode;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public interface PlatformBlockAccess {
    PlatformBlockAccess INSTANCE = Services.load(PlatformBlockAccess.class);

    static PlatformBlockAccess getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the light emission of the current block.
     * @param state The current block
     * @param level The current level slice
     * @param pos The block position
     * @return The light emission of the current block (default 0)
     */
    int getLightEmission(BlockState state, BlockView level, BlockPos pos);

    /**
     * Checks if the block should stop drawing a specific side based on the adjacent block.
     * @param level The level slice.
     * @param selfState The block currently being drawn.
     * @param otherState The adjacent block.
     * @param selfPos The current block position.
     * @param otherPos The other block position.
     * @param facing The direction between the two blocks.
     * @return If the block's face should be skipped.
     */
    boolean shouldSkipRender(BlockView level, BlockState selfState, BlockState otherState, BlockPos selfPos, BlockPos otherPos, Direction facing);

    /**
     * Returns if the fluid should render fluid overlays if a block is adjacent to it.
     * @param block The block adjacent to the fluid being rendered
     * @param level The level slice
     * @param pos The position of the adjacent block
     * @param fluidState The fluid
     * @return True if the fluid should render an overlay.
     */
    boolean shouldShowFluidOverlay(BlockState block, BlockView level, BlockPos pos, BlockState fluidState);

    /**
     * @return If the platform can return block entity data
     */
    boolean platformHasBlockData();

    /**
     * Gets the block shade.
     * @param quad The quad being rendered.
     * @param level The level.
     * @param shade If directional lighting should be added.
     * @return the block shade
     */
    float getNormalVectorShade(ModelQuadView quad, LevelSlice level, boolean shade);

    /**
     * If the block contains forced ambient occlusion.
     * @param model The model being rendered
     * @param state The current block
     * @param data Any model data
     * @param renderType The current render type being drawn
     * @param level The level slice
     * @param pos The current position
     * @return If ambient occlusion is forced, or {@code DEFAULT}
     */
    AmbientOcclusionMode usesAmbientOcclusion(BakedModel model, BlockState state, SodiumModelData data, RenderLayer renderType, LevelSlice level, BlockPos pos);

    /**
     * @param blockEntity The block entity to check.
     * @param player The current player rendering.
     * @return Whether this block entity should activate the outline shader.
     */
    boolean shouldBlockEntityGlow(BlockEntity blockEntity, ClientPlayerEntity player);

    /**
     * Determines if a fluid adjacent to the block on the given side should not be rendered.
     *
     * @param adjDirection      the face of this block that the fluid is adjacent to
     * @param fluid the fluid that is touching that face
     * @return if this block should cause the fluid's face to not render
     */
    boolean shouldOccludeFluid(Direction adjDirection, BlockState adjBlockState, BlockState fluid);
}
