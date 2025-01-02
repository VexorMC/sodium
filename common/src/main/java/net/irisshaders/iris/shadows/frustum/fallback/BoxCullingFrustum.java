package net.irisshaders.iris.shadows.frustum.fallback;

import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.minecraft.client.render.BaseFrustum;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class BoxCullingFrustum extends Frustum implements net.caffeinemc.mods.sodium.client.render.viewport.frustum.Frustum, ViewportProvider {
	private final BoxCuller boxCuller;
	private final Vector3d position = new Vector3d();

	public BoxCullingFrustum(BoxCuller boxCuller) {
		this.boxCuller = boxCuller;
	}

	public void prepare(double cameraX, double cameraY, double cameraZ) {
		this.position.set(cameraX, cameraY, cameraZ);
		boxCuller.setPosition(cameraX, cameraY, cameraZ);
	}

    @Override
    public boolean isInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return !boxCuller.isCulled(new Box(minX, minY, minZ, maxX, maxY, maxZ));
    }

	@Override
	public Viewport sodium$createViewport(double tickDelta) {
		return new Viewport(this, new Vec3d(position.x, position.y, position.z));
	}

	@Override
	public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return !boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
