package net.caffeinemc.mods.sodium.client.services;

import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import dev.vexor.radium.compat.mojang.minecraft.random.RandomSource;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface PlatformModelAccess {
    PlatformModelAccess INSTANCE = Services.load(PlatformModelAccess.class);

    static PlatformModelAccess getInstance() {
        return INSTANCE;
    }

    /**
     * Returns all the render types used by this model.
     * @param level The level slice.
     * @param model The {@code BakedModel} currently being drawn.
     * @param state The block state of the current block.
     * @param pos The position of the block being rendered.
     * @param random The random source used by the current block renderer.
     * @param modelData The platform specific model data.
     * @return A list of render types used by this model.
     */
    Iterable<RenderLayer> getModelRenderTypes(BlockView level, BakedModel model, BlockState state, BlockPos pos, RandomSource random, SodiumModelData modelData);

    /**
     * Returns a list of quads used by this model.
     * @param level The level slice.
     * @param pos The position of the block being rendered.
     * @param model The {@code BakedModel} currently being drawn.
     * @param state The block state of the current block.
     * @param face The current face of the block being rendered, or null if rendering unassigned quads.
     * @param random The random source used by the current block renderer.
     * @param renderType The current render type being drawn.
     * @param modelData The platform specific model data.
     * @return The list of quads used by the model.
     */
    List<BakedQuad> getQuads(BlockView level, BlockPos pos, BakedModel model, BlockState state, Direction face, RandomSource random, RenderLayer renderType, SodiumModelData modelData);

    /**
     * Gets the container holding model data for this chunk. <b>This operation is not thread safe.</b>
     * @param level The current vanilla Level.
     * @param sectionPos The current chunk position.
     * @return The model data container for this section
     */
    SodiumModelDataContainer getModelDataContainer(World level, SectionPos sectionPos);

    /**
     * Gets the true model data from the block data in the container.
     * @param slice The current world slice.
     * @param model The current model.
     * @param state The current block.
     * @param pos The current block position.
     * @param originalData The model data, as retrieved by {@code getModelDataContainer()}.
     * @return The true model data, to render with.
     */
    SodiumModelData getModelData(LevelSlice slice, BakedModel model, BlockState state, BlockPos pos, SodiumModelData originalData);

    /**
     * Should not use. <b>Use {@code SodiumModelData.EMPTY} instead.</b>
     * @return The empty model data for this platform.
     */
    @ApiStatus.Internal
    SodiumModelData getEmptyModelData();
}
