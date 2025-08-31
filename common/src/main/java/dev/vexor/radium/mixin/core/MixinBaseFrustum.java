package dev.vexor.radium.mixin.core;

import net.caffeinemc.mods.sodium.client.util.frustum.ExtendedFrustum;
import net.minecraft.client.render.BaseFrustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BaseFrustum.class)
public abstract class MixinBaseFrustum implements ExtendedFrustum {
    @Shadow
    public float[][] homogeneousCoordinates;

    @Shadow
    protected abstract double multiply(float[] frustum, double x, double y, double z);

    @Override
    /**
     * Tests an AABB (axis-aligned bounding box) against this frustum.
     *
     * @param minX minimum x of the box
     * @param minY minimum y of the box
     * @param minZ minimum z of the box
     * @param maxX maximum x of the box
     * @param maxY maximum y of the box
     * @param maxZ maximum z of the box
     * @return 0 if the box is completely outside,
     *         1 if it intersects,
     *         2 if it is completely inside
     */
    public int radium$intersect(double minX, double minY, double minZ,
                                double maxX, double maxY, double maxZ) {
        boolean intersects = false;

        for (int i = 0; i < 6; i++) {
            float[] plane = this.homogeneousCoordinates[i];

            // Count how many corners are in front of this plane
            int inCount = 0;
            if (this.multiply(plane, minX, minY, minZ) > 0.0) inCount++;
            if (this.multiply(plane, maxX, minY, minZ) > 0.0) inCount++;
            if (this.multiply(plane, minX, maxY, minZ) > 0.0) inCount++;
            if (this.multiply(plane, maxX, maxY, minZ) > 0.0) inCount++;
            if (this.multiply(plane, minX, minY, maxZ) > 0.0) inCount++;
            if (this.multiply(plane, maxX, minY, maxZ) > 0.0) inCount++;
            if (this.multiply(plane, minX, maxY, maxZ) > 0.0) inCount++;
            if (this.multiply(plane, maxX, maxY, maxZ) > 0.0) inCount++;

            if (inCount == 0) {
                return 0;
            } else if (inCount < 8) {
                intersects = true;
            }
        }

        return intersects ? 1 : 2;
    }

}
