package net.caffeinemc.mods.sodium.mixin.core.render.frustum;

import net.caffeinemc.mods.sodium.client.render.viewport.frustum.SimpleFrustum;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Frustum.class)
public class FrustumMixin implements ViewportProvider {
    @Override
    public Viewport sodium$createViewport() {
        return new Viewport(new SimpleFrustum((Frustum) (Object) this), new Vector3d(
                Camera.getPosition().x, Camera.getPosition().y, Camera.getPosition().z
        ));
    }
}
