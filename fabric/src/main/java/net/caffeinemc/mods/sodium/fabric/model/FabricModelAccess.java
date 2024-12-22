package net.caffeinemc.mods.sodium.fabric.model;

import dev.vexor.radium.compat.mojang.minecraft.random.RandomSource;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.caffeinemc.mods.sodium.client.services.PlatformModelAccess;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.caffeinemc.mods.sodium.client.services.SodiumModelDataContainer;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FabricModelAccess implements PlatformModelAccess {
    private static final SodiumModelDataContainer EMPTY_CONTAINER = new SodiumModelDataContainer(Long2ObjectMaps.emptyMap());

    @Override
    public Iterable<RenderLayer> getModelRenderTypes(BlockView level, BakedModel model, BlockState state, BlockPos pos, RandomSource random, SodiumModelData modelData) {
        return List.of(state.getBlock().getRenderLayerType());
    }

    @Override
    public List<BakedQuad> getQuads(BlockView level, BlockPos pos, BakedModel model, BlockState state, Direction face, RandomSource random, RenderLayer renderType, SodiumModelData modelData) {
        if (face == null) return model.getQuads();

        return model.getByDirection(face);
    }

    @Override
    public SodiumModelDataContainer getModelDataContainer(World level, SectionPos sectionPos) {
        return EMPTY_CONTAINER;
    }

    @Override
    public SodiumModelData getModelData(LevelSlice slice, BakedModel model, BlockState state, BlockPos pos, SodiumModelData originalData) {
        return SodiumModelData.EMPTY;
    }

    @Override
    public SodiumModelData getEmptyModelData() {
        return SodiumModelData.EMPTY;
    }
}
