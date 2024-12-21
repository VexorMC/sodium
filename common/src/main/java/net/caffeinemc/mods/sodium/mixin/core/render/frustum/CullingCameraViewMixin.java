package net.caffeinemc.mods.sodium.mixin.core.render.frustum;

import net.caffeinemc.mods.sodium.client.render.viewport.frustum.SimpleFrustum;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.CullingCameraView;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CullingCameraView.class)
public abstract class CullingCameraViewMixin implements ViewportProvider {
    @Shadow public abstract boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    /**
     * @reason Avoid passing infinite extents box into optimized frustum code.
     * @author XFactHD (ported by embeddedt)
     */
    @Overwrite
    public boolean isBoxInFrustum(Box box) {
        return this.isBoxInFrustum(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    @Override
    public Viewport sodium$createViewport() {
        Vec3d pos = MinecraftClient.getInstance().getCameraEntity().getPos();
        return new Viewport(new SimpleFrustum((CullingCameraView) (Object) this), new Vector3d(
                pos.x, pos.y, pos.z
        ));
    }
}
