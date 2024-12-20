package net.caffeinemc.mods.sodium.fabric.level;

import net.caffeinemc.mods.sodium.client.services.PlatformLevelAccess;
import net.caffeinemc.mods.sodium.client.world.SodiumAuxiliaryLightManager;
import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

public class FabricLevelAccess implements PlatformLevelAccess {
    @Override
    public @Nullable Object getBlockEntityData(BlockEntity blockEntity) {
        return blockEntity.getDataValue();
    }

    @Override
    public @Nullable SodiumAuxiliaryLightManager getLightManager(Chunk chunk, SectionPos pos) {
        return null;
    }
}
