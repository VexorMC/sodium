package net.caffeinemc.mods.sodium.fabric.level;

import net.caffeinemc.mods.sodium.client.services.PlatformLevelAccess;
import net.caffeinemc.mods.sodium.client.world.SodiumAuxiliaryLightManager;
import dev.lunasa.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Function;

public class FabricLevelAccess implements PlatformLevelAccess {
    @Override
    public @Nullable Object getBlockEntityData(BlockEntity blockEntity) {
        return blockEntity.getDataValue();
    }

    @Override
    public @Nullable SodiumAuxiliaryLightManager getLightManager(LevelChunk chunk, SectionPos pos) {
        return null;
    }
}
