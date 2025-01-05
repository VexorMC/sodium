package net.coderbot.iris.shadows.frustum.fallback;

import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;

public class BoxCullingFrustum extends Frustum {
	private final BoxCuller boxCuller;

	public BoxCullingFrustum(BoxCuller boxCuller) {
		this.boxCuller = boxCuller;
	}

	@Override
	public void start() {
        double cameraX = MinecraftClient.getInstance().getCameraEntity().getPos().x;
        double cameraY = MinecraftClient.getInstance().getCameraEntity().getPos().y;
        double cameraZ = MinecraftClient.getInstance().getCameraEntity().getPos().z;
        boxCuller.setPosition(cameraX, cameraY, cameraZ);
	}

	// for Sodium
	// TODO: Better way to do this... Maybe we shouldn't be using a frustum for the box culling in the first place!
	public boolean fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return !boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ);
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
        return !boxCuller.isCulled((float)minX, (float)minY, (float)minZ, (float)maxX, (float)maxY, (float)maxZ);
    }
}
