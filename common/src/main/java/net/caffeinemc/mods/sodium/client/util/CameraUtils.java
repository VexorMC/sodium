package net.caffeinemc.mods.sodium.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CameraUtils {
    private static final BlockPos.Mutable blockPosition = new BlockPos.Mutable();

    public static BlockPos getBlockPosition() {
        Vec3d rawPosition = MinecraftClient.getInstance().getCameraEntity().getPos();
        blockPosition.setPosition((int)rawPosition.x, (int)rawPosition.y, (int)rawPosition.z);
        return blockPosition;
    }
}
