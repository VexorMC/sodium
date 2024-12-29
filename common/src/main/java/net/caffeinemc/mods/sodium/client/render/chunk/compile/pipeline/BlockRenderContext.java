package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class BlockRenderContext {
    private final LevelSlice slice;
    public final TranslucentGeometryCollector collector;

    private final BlockPos.Mutable pos = new BlockPos.Mutable();

    private final Vector3f origin = new Vector3f();

    private BlockState state;
    private BakedModel model;

    public BlockRenderContext(LevelSlice slice, TranslucentGeometryCollector collector) {
        this.slice = slice;
        this.collector = collector;
    }

    public void update(BlockPos pos, BlockPos origin, BlockState state, BakedModel model) {
        this.pos.setPosition(pos.getX(), pos.getY(), pos.getZ());
        this.origin.set(origin.getX(), origin.getY(), origin.getZ());

        this.state = state;
        this.model = model;
    }

    /**
     * @return The collector for translucent geometry sorting
     */
    public TranslucentGeometryCollector collector() {
        return this.collector;
    }

    /**
     * @return The position (in block space) of the block being rendered
     */
    public BlockPos pos() {
        return this.pos;
    }

    /**
     * @return The level which the block is being rendered from
     */
    public LevelSlice slice() {
        return this.slice;
    }

    /**
     * @return The state of the block being rendered
     */
    public BlockState state() {
        return this.state;
    }

    /**
     * @return The model used for this block
     */
    public BakedModel model() {
        return this.model;
    }

    /**
     * @return The origin of the block within the model
     */
    public Vector3fc origin() {
        return this.origin;
    }
}
