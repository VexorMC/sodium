package net.caffeinemc.mods.sodium.client.render.viewport.frustum;

import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.util.frustum.ExtendedFrustum;
import net.minecraft.client.render.CullingCameraView;

public final class SimpleFrustum implements Frustum {
    private final CullingCameraView frustum;
    private final ExtendedFrustum extendedFrustum;

    public SimpleFrustum(CullingCameraView frustum) {
        this.frustum = frustum;
        this.extendedFrustum = (ExtendedFrustum) frustum;
    }

    @Override
    public boolean testSection(float x, float y, float z) {
        return this.testSectionExpanded(x, y, z, 0.0f);
    }

    @Override
    public boolean testSectionExpanded(float x, float y, float z, float extend) {
        float radius = Viewport.CHUNK_SECTION_PADDED_RADIUS + extend;

        return this.frustum.isBoxInFrustum(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );
    }

    @Override
    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.frustum.isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.extendedFrustum.radium$intersect(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
