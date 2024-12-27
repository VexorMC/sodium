package net.caffeinemc.mods.sodium.mixin.core.render.frustum;

import net.caffeinemc.mods.sodium.client.render.viewport.frustum.SimpleFrustum;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.CullingCameraView;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CullingCameraView.class)
public abstract class CullingCameraViewMixin implements ViewportProvider {
   // @Override
   // public Viewport sodium$createViewport(double tickDelta) {
   //     Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
//
   //     double x = cameraEntity.prevTickX + (cameraEntity.x - cameraEntity.prevTickX) * tickDelta;
   //     double y = cameraEntity.prevTickY + (cameraEntity.y - cameraEntity.prevTickY) * tickDelta;// + (double) cameraEntity.getEyeHeight();
   //     double z = cameraEntity.prevTickZ + (cameraEntity.z - cameraEntity.prevTickZ) * tickDelta;
//
   //     return new Viewport(new SimpleFrustum((CullingCameraView) (Object) this), new Vector3d(x, y, z));
   // }
}
