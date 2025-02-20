package net.caffeinemc.mods.sodium.api.math;

import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.minecraft.util.math.Direction;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Implements optimized utilities for transforming vectors with a given matrix.
 *
 * Note: Brackets must be used carefully in the transform functions to ensure that floating-point errors are
 * the same as those produced by JOML, otherwise Z-fighting will occur.
 */
public class MatrixHelper {
    /**
     * @param mat The transformation matrix to apply to the normal
     * @param skipNormalization Whether normalizing the vector is unnecessary
     * @param x The X-coordinate of the normal vector
     * @param y The Y-coordinate of the normal vector
     * @param z The Z-coordinate of the normal vector
     * @return The transformed normal vector (in packed format)
     */
    public static int transformNormal(Matrix3f mat, boolean skipNormalization, float x, float y, float z) {
        // The transformed normal vector
        float nxt = transformNormalX(mat, x, y, z);
        float nyt = transformNormalY(mat, x, y, z);
        float nzt = transformNormalZ(mat, x, y, z);

        if (!skipNormalization) {
            float scalar = Math.invsqrt(Math.fma(nxt, nxt, Math.fma(nyt, nyt, nzt * nzt)));

            nxt *= scalar;
            nyt *= scalar;
            nzt *= scalar;
        }

        return NormI8.pack(nxt, nyt, nzt);
    }

    /**
     * @param mat The transformation matrix to apply to the normal
     * @param x The X-coordinate of the normal vector
     * @param y The Y-coordinate of the normal vector
     * @param z The Z-coordinate of the normal vector
     * @return The transformed normal vector (in packed format)
     */
    public static int transformSafeNormal(Matrix3f mat, float x, float y, float z) {
        // The transformed normal vector
        float nxt = transformNormalX(mat, x, y, z);
        float nyt = transformNormalY(mat, x, y, z);
        float nzt = transformNormalZ(mat, x, y, z);

        return NormI8.pack(nxt, nyt, nzt);
    }

    /**
     * @param mat The transformation matrix to apply to the normal
     * @param norm The normal vector to transform (in packed format)
     * @return The transformed normal vector (in packed format)
     */
    public static int transformNormal(Matrix3f mat, boolean skipNormalization, int norm) {
        // The unpacked normal vector
        float x = NormI8.unpackX(norm);
        float y = NormI8.unpackY(norm);
        float z = NormI8.unpackZ(norm);

        return transformNormal(mat, skipNormalization, x, y, z);
    }

    /**
     * @param mat The transformation matrix to apply to the normal
     * @param norm The normal vector to transform (in packed format)
     * @return The transformed normal vector (in packed format)
     */
    public static int transformNormal(Matrix3f mat, boolean skipNormalization, Direction norm) {
        return transformNormal(mat, skipNormalization, norm.getVector().getX(), norm.getVector().getY(), norm.getVector().getZ());
    }

    /**
     * @param mat The transformation matrix to apply to the normal vector
     * @param x The X-coordinate of the normal vector
     * @param y The Y-coordinate of the normal vector
     * @param z The Z-coordinate of the normal vector
     * @return The transformed X-coordinate for the normal vector
     */
    public static float transformNormalX(Matrix3f mat, float x, float y, float z) {
        return (mat.m00() * x) + ((mat.m10() * y) + (mat.m20() * z));
    }

    /**
     * @param mat The transformation matrix to apply to the normal vector
     * @param x The X-coordinate of the normal vector
     * @param y The Y-coordinate of the normal vector
     * @param z The Z-coordinate of the normal vector
     * @return The transformed Y-coordinate for the normal vector
     */
    public static float transformNormalY(Matrix3f mat, float x, float y, float z) {
        return (mat.m01() * x) + ((mat.m11() * y) + (mat.m21() * z));
    }

    /**
     * @param mat The transformation matrix to apply to the normal vector
     * @param x The X-coordinate of the normal vector
     * @param y The Y-coordinate of the normal vector
     * @param z The Z-coordinate of the normal vector
     * @return The transformed Z-coordinate for the normal vector
     */
    public static float transformNormalZ(Matrix3f mat, float x, float y, float z) {
        return (mat.m02() * x) + ((mat.m12() * y) + (mat.m22() * z));
    }

    /**
     * @param mat The transformation matrix to apply to the vertex position
     * @param x The X-coordinate of the vertex position
     * @param y The Y-coordinate of the vertex position
     * @param z The Z-coordinate of the vertex position
     * @return The transformed X-coordinate for the vertex position
     */
    public static float transformPositionX(Matrix4f mat, float x, float y, float z) {
        return (mat.m00() * x) + ((mat.m10() * y) + ((mat.m20() * z) + mat.m30()));
    }

    /**
     * @param mat The transformation matrix to apply to the vertex position
     * @param x The X-coordinate of the vertex position
     * @param y The Y-coordinate of the vertex position
     * @param z The Z-coordinate of the vertex position
     * @return The transformed Y-coordinate for the vertex position
     */
    public static float transformPositionY(Matrix4f mat, float x, float y, float z) {
        return (mat.m01() * x) + ((mat.m11() * y) + ((mat.m21() * z) + mat.m31()));
    }

    /**
     * @param mat The transformation matrix to apply to the vertex position
     * @param x The X-coordinate of the vertex position
     * @param y The Y-coordinate of the vertex position
     * @param z The Z-coordinate of the vertex position
     * @return The transformed Z-coordinate for the vertex position
     */
    public static float transformPositionZ(Matrix4f mat, float x, float y, float z) {
        return (mat.m02() * x) + ((mat.m12() * y) + ((mat.m22() * z) + mat.m32()));
    }


}
