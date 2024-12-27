package net.caffeinemc.mods.sodium.client.render.viewport.frustum;

import net.minecraft.client.render.CullingCameraView;

public final class SimpleFrustum implements Frustum {
    private final CullingCameraView frustum;

    public SimpleFrustum(CullingCameraView frustumIntersection) {
        this.frustum = frustumIntersection;
    }

    @Override
    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return frustum.isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
