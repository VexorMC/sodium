package net.caffeinemc.mods.sodium.client.render.viewport.frustum;

public final class SimpleFrustum implements Frustum {
    private final net.minecraft.client.render.Frustum frustum;

    public SimpleFrustum(net.minecraft.client.render.Frustum frustumIntersection) {
        this.frustum = frustumIntersection;
    }

    @Override
    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.frustum.isInFrustum(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
