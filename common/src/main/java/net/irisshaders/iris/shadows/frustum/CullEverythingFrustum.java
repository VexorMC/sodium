package net.irisshaders.iris.shadows.frustum;

import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class CullEverythingFrustum extends Frustum implements ViewportProvider, net.caffeinemc.mods.sodium.client.render.viewport.frustum.Frustum {
	private final Vector3d position = new Vector3d();

	public CullEverythingFrustum() {
	}


    @Override
    public boolean isInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return false;
    }

    @Override
    public Viewport sodium$createViewport(double tickDelta) {
        return new Viewport(this, new Vec3d(position.x, position.y, position.z));
    }

	@Override
	public boolean testAab(float v, float v1, float v2, float v3, float v4, float v5) {
		return false;
	}
}
