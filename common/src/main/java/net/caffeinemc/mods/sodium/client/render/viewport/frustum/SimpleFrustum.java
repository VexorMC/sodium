package net.caffeinemc.mods.sodium.client.render.viewport.frustum;

import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.util.frustum.ExtendedFrustum;
import net.minecraft.client.render.CullingCameraView;
import org.joml.FrustumIntersection;

public final class SimpleFrustum implements Frustum {
    private float nxX, nxY, nxZ, negNxW;
    private float pxX, pxY, pxZ, negPxW;
    private float nyX, nyY, nyZ, negNyW;
    private float pyX, pyY, pyZ, negPyW;
    private float nzX, nzY, nzZ, negNzW;
    private float pzX, pzY, pzZ, negPzW;

    private final ExtendedFrustum extendedFrustum;

    public SimpleFrustum(CullingCameraView frustum) {
        this.extendedFrustum = (ExtendedFrustum) frustum;

        float[][] planes = this.extendedFrustum.radium$getPlanes();

        nxX = planes[0][0]; nxY = planes[0][1]; nxZ = planes[0][2];
        pxX = planes[1][0]; pxY = planes[1][1]; pxZ = planes[1][2];
        nyX = planes[2][0]; nyY = planes[2][1]; nyZ = planes[2][2];
        pyX = planes[3][0]; pyY = planes[3][1]; pyZ = planes[3][2];
        nzX = planes[4][0]; nzY = planes[4][1]; nzZ = planes[4][2];
        pzX = planes[5][0]; pzY = planes[5][1]; pzZ = planes[5][2];

        float size = Viewport.CHUNK_SECTION_PADDED_RADIUS;

        negNxW = negW(planes[0], size);
        negPxW = negW(planes[1], size);
        negNyW = negW(planes[2], size);
        negPyW = negW(planes[3], size);
        negNzW = negW(planes[4], size);
        negPzW = negW(planes[5], size);
    }

    private static float negW(float[] p, float size) {
        return -(p[3]
                + p[0] * (p[0] < 0 ? -size : size)
                + p[1] * (p[1] < 0 ? -size : size)
                + p[2] * (p[2] < 0 ? -size : size));
    }

    @Override
    public boolean testSection(float x, float y, float z) {
        return nxX * x + nxY * y + nxZ * z >= negNxW &&
                pxX * x + pxY * y + pxZ * z >= negPxW &&
                nyX * x + nyY * y + nyZ * z >= negNyW &&
                pyX * x + pyY * y + pyZ * z >= negPyW &&
                nzX * x + nzY * y + nzZ * z >= negNzW &&
                pzX * x + pzY * y + pzZ * z >= negPzW;
    }

    @Override
    public boolean testSectionExpanded(float x, float y, float z, float extend) {
        float minX = x - extend, maxX = x + extend;
        float minY = y - extend, maxY = y + extend;
        float minZ = z - extend, maxZ = z + extend;

        return nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) >= negNxW &&
                pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) >= negPxW &&
                nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) >= negNyW &&
                pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) >= negPyW &&
                nzX * (nzX < 0 ? minX : maxX) + nzY * (nzY < 0 ? minY : maxY) + nzZ * (nzZ < 0 ? minZ : maxZ) >= negNzW &&
                pzX * (pzX < 0 ? minX : maxX) + pzY * (pzY < 0 ? minY : maxY) + pzZ * (pzZ < 0 ? minZ : maxZ) >= negPzW;
    }

    @Override
    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.intersectAab(minX, minY, minZ, maxX, maxY, maxZ) != FrustumIntersection.OUTSIDE;
    }

    @Override
    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.extendedFrustum.radium$intersect(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
