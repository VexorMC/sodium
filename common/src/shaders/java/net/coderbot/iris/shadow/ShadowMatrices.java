package net.coderbot.iris.shadow;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.vendored.joml.Matrix4f;
import net.coderbot.iris.vendored.joml.Quaternionf;

public class ShadowMatrices {
    private static final float NEAR = 0.05f;
    private static final float FAR = 256.0f;

    // NB: These matrices are in column-major order, not row-major order like what you'd expect!

    public static Matrix4f createOrthoMatrix(float halfPlaneLength) {
        return new Matrix4f(
                // column 1
                1.0f / halfPlaneLength, 0f, 0f, 0f,
                // column 2
                0f, 1.0f / halfPlaneLength, 0f, 0f,
                // column 3
                0f, 0f, 2.0f / (NEAR - FAR), 0f,
                // column 4
                0f, 0f, -(FAR + NEAR) / (FAR - NEAR), 1f
        );
    }

    public static Matrix4f createPerspectiveMatrix(float fov) {
        // This converts from degrees to radians.
        final float yScale = (float) (1.0f / Math.tan(Math.toRadians(fov) * 0.5f));
        return new Matrix4f(
                // column 1
                yScale, 0f, 0f, 0f,
                // column 2
                0f, yScale, 0f, 0f,
                // column 3
                0f, 0f, (FAR + NEAR) / (NEAR - FAR), -1.0F,
                // column 4
                0f, 0f, 2.0F * FAR * NEAR / (NEAR - FAR), 1f
        );
    }

    public static void createBaselineModelViewMatrix(PoseStack target, float shadowAngle, float sunPathRotation) {
        final float skyAngle;

        if (shadowAngle < 0.25f) {
            skyAngle = shadowAngle + 0.75f;
        } else {
            skyAngle = shadowAngle - 0.25f;
        }

        target.last().normal().identity();
        target.last().pose().identity();

        target.last().pose().translate(0.0f, 0.0f, -100.0f);
        Quaternionf quat = new Quaternionf();
        quat.rotateX((float)Math.toRadians(90F));
        quat.rotateZ((float)Math.toRadians(skyAngle * -360.0f));
        quat.rotateX((float)Math.toRadians(sunPathRotation));
        target.mulPose(quat);
    }

    public static void snapModelViewToGrid(PoseStack target, float shadowIntervalSize, double cameraX, double cameraY, double cameraZ) {
        if (Math.abs(shadowIntervalSize) == 0.0F) {
            // Avoid a division by zero - semantically, this just means that the snapping does not take place,
            // if the shadow interval (size of each grid "cell") is zero.
            return;
        }

        // Calculate where we are within each grid "cell"
        // These values will be in the range of (-shadowIntervalSize, shadowIntervalSize)
        //
        // It looks like it's intended for these to be within the range [0, shadowIntervalSize), however since the
        // expression (-2.0f % 32.0f) returns -2.0f, negative inputs will result in negative outputs.
        float offsetX = (float) cameraX % shadowIntervalSize;
        float offsetY = (float) cameraY % shadowIntervalSize;
        float offsetZ = (float) cameraZ % shadowIntervalSize;

        // Halve the size of each grid cell in order to move to the center of it.
        final float halfIntervalSize = shadowIntervalSize / 2.0f;

        // Shift by -halfIntervalSize
        //
        // It's clear that the intent of the algorithm was to place the values into the range:
        // [-shadowIntervalSize/2, shadowIntervalSize), however due to the previously-mentioned behavior with negatives,
        // it's possible that values will lie in the range (-3shadowIntervalSize/2, shadowIntervalSize/2).
        offsetX -= halfIntervalSize;
        offsetY -= halfIntervalSize;
        offsetZ -= halfIntervalSize;

        target.last().pose().translate(offsetX, offsetY, offsetZ);
    }

    public static void createModelViewMatrix(PoseStack target, float shadowAngle, float shadowIntervalSize, float sunPathRotation, double cameraX, double cameraY, double cameraZ) {
        createBaselineModelViewMatrix(target, shadowAngle, sunPathRotation);
        snapModelViewToGrid(target, shadowIntervalSize, cameraX, cameraY, cameraZ);
    }
}