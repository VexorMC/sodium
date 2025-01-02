package net.irisshaders.iris.shadows.frustum.advanced;

import net.caffeinemc.mods.sodium.client.render.viewport.frustum.Frustum;
import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public class ReversedAdvancedShadowCullingFrustum extends AdvancedShadowCullingFrustum implements Frustum {
	private final BoxCuller distanceCuller;

	public ReversedAdvancedShadowCullingFrustum(Matrix4fc modelViewProjection, Matrix4fc shadowProjection, Vector3f shadowLightVectorFromOrigin, BoxCuller voxelCuller, BoxCuller distanceCuller) {
		super(modelViewProjection, shadowProjection, shadowLightVectorFromOrigin, voxelCuller);
		this.distanceCuller = distanceCuller;
	}

	@Override
	public void start() {
		if (this.distanceCuller != null) {
			this.distanceCuller.setPosition(MinecraftClient.getInstance().getCameraEntity().x, MinecraftClient.getInstance().getCameraEntity().y, MinecraftClient.getInstance().getCameraEntity().z);
		}
		super.start();
	}

    @Override
    public boolean isInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (distanceCuller != null && distanceCuller.isCulled(new Box(minX, minY, minZ, maxX, maxY, maxZ))) {
            return false;
        }

        if (boxCuller != null && !boxCuller.isCulled(new Box(minX, minY, minZ, maxX, maxY, maxZ))) {
            return true;
        }

        return isVisible(minX, minY, minZ, maxX, maxY, maxZ) != 0;
    }

	@Override
	public int fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		if (distanceCuller != null && distanceCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ)) {
			return 0;
		}

		if (boxCuller != null && !boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ)) {
			return 2;
		}

		return isVisible(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		if (distanceCuller != null && distanceCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ)) {
			return false;
		}

		if (boxCuller != null && !boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ)) {
			return true;
		}

		return this.checkCornerVisibility(minX, minY, minZ, maxX, maxY, maxZ) > 0;
	}
}
