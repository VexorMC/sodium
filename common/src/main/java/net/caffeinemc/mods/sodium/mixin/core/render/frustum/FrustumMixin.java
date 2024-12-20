package net.caffeinemc.mods.sodium.mixin.core.render.frustum;

import net.caffeinemc.mods.sodium.client.render.viewport.frustum.SimpleFrustum;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Frustum.class)
public class FrustumMixin implements ViewportProvider {
    @Override
    public Viewport sodium$createViewport() {
        Vec3d pos = MinecraftClient.getInstance().getCameraEntity().getPos();
        return new Viewport(new SimpleFrustum((Frustum) (Object) this), new Vector3d(
                pos.x, pos.y, pos.z
        ));
    }
}
