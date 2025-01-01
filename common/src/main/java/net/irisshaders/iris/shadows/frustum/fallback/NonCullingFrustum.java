package net.irisshaders.iris.shadows.frustum.fallback;

import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class NonCullingFrustum extends Frustum implements ViewportProvider, net.caffeinemc.mods.sodium.client.render.viewport.frustum.Frustum {
	private final Vector3d position = new Vector3d();

	public NonCullingFrustum() {
	}

	// For Immersive Portals
	// NB: The shadow culling in Immersive Portals must be disabled, because when Advanced Shadow Frustum Culling
	//     is not active, we are at a point where we can make no assumptions how the shader pack uses the shadow
	//     pass beyond what it already tells us. So we cannot use any extra fancy culling methods.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

    @Override
    public boolean isInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return true;
    }

    @Override
	public void start() {
		this.position.set(
                MinecraftClient.getInstance().getCameraEntity().x,
                MinecraftClient.getInstance().getCameraEntity().y,
                MinecraftClient.getInstance().getCameraEntity().z
        );
	}

	@Override
	public Viewport sodium$createViewport(double tickDelta) {
		return new Viewport(this, new Vec3d(position.x, position.y, position.z));
	}

	@Override
	public boolean testAab(float v, float v1, float v2, float v3, float v4, float v5) {
		return true;
	}
}
