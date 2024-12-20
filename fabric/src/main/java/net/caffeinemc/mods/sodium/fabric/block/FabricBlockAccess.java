package net.caffeinemc.mods.sodium.fabric.block;

import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AmbientOcclusionMode;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class FabricBlockAccess implements PlatformBlockAccess {
    @Override
    public int getLightEmission(BlockState state, BlockView level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean shouldSkipRender(BlockView level, BlockState selfState, BlockState otherState, BlockPos selfPos, BlockPos otherPos, Direction facing) {
        return false;
    }

    @Override
    public boolean shouldShowFluidOverlay(BlockState block, BlockView level, BlockPos pos, BlockState fluidState) {
        return false;
    }

    @Override
    public boolean platformHasBlockData() {
        return false;
    }

    @Override
    public float getNormalVectorShade(ModelQuadView quad, LevelSlice level, boolean shade) {
        return 0;
    }

    @Override
    public AmbientOcclusionMode usesAmbientOcclusion(BakedModel model, BlockState state, SodiumModelData data, RenderLayer renderType, LevelSlice level, BlockPos pos) {
        if (MinecraftClient.isAmbientOcclusionEnabled()) {
            return AmbientOcclusionMode.ENABLED;
        }

        return AmbientOcclusionMode.DISABLED;
    }

    @Override
    public boolean shouldBlockEntityGlow(BlockEntity blockEntity, ClientPlayerEntity player) {
        return false;
    }

    @Override
    public boolean shouldOccludeFluid(Direction adjDirection, BlockState adjBlockState, BlockState fluid) {
        return false;
    }
}
