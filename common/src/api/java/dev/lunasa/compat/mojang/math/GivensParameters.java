package dev.lunasa.compat.mojang.math;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

public class GivensParameters {
    private final float sinHalf;
    private final float cosHalf;

    public GivensParameters(float sinHalf, float cosHalf) {
        this.sinHalf = sinHalf;
        this.cosHalf = cosHalf;
    }

    public float getSinHalf() {
        return sinHalf;
    }

    public float getCosHalf() {
        return cosHalf;
    }

    public static GivensParameters fromUnnormalized(float f, float f2) {
        float f3 = Math.invsqrt(f * f + f2 * f2);
        return new GivensParameters(f3 * f, f3 * f2);
    }

    public static GivensParameters fromPositiveAngle(float f) {
        float f2 = Math.sin(f / 2.0f);
        float f3 = Math.cosFromSin(f2, f / 2.0f);
        return new GivensParameters(f2, f3);
    }

    public GivensParameters inverse() {
        return new GivensParameters(-this.sinHalf, this.cosHalf);
    }

    public Quaternionf aroundX(Quaternionf quaternionf) {
        return quaternionf.set(this.sinHalf, 0.0f, 0.0f, this.cosHalf);
    }

    public Quaternionf aroundY(Quaternionf quaternionf) {
        return quaternionf.set(0.0f, this.sinHalf, 0.0f, this.cosHalf);
    }

    public Quaternionf aroundZ(Quaternionf quaternionf) {
        return quaternionf.set(0.0f, 0.0f, this.sinHalf, this.cosHalf);
    }

    public float cos() {
        return this.cosHalf * this.cosHalf - this.sinHalf * this.sinHalf;
    }

    public float sin() {
        return 2.0f * this.sinHalf * this.cosHalf;
    }

    public Matrix3f aroundX(Matrix3f matrix3f) {
        matrix3f.m01 = 0.0f;
        matrix3f.m02 = 0.0f;
        matrix3f.m10 = 0.0f;
        matrix3f.m20 = 0.0f;
        float f = this.cos();
        float f2 = this.sin();
        matrix3f.m11 = f;
        matrix3f.m22 = f;
        matrix3f.m12 = f2;
        matrix3f.m21 = -f2;
        matrix3f.m00 = 1.0f;
        return matrix3f;
    }

    public Matrix3f aroundY(Matrix3f matrix3f) {
        matrix3f.m01 = 0.0f;
        matrix3f.m10 = 0.0f;
        matrix3f.m12 = 0.0f;
        matrix3f.m21 = 0.0f;
        float f = this.cos();
        float f2 = this.sin();
        matrix3f.m00 = f;
        matrix3f.m22 = f;
        matrix3f.m02 = -f2;
        matrix3f.m20 = f2;
        matrix3f.m11 = 1.0f;
        return matrix3f;
    }

    public Matrix3f aroundZ(Matrix3f matrix3f) {
        matrix3f.m02 = 0.0f;
        matrix3f.m12 = 0.0f;
        matrix3f.m20 = 0.0f;
        matrix3f.m21 = 0.0f;
        float f = this.cos();
        float f2 = this.sin();
        matrix3f.m00 = f;
        matrix3f.m11 = f;
        matrix3f.m01 = f2;
        matrix3f.m10 = -f2;
        matrix3f.m22 = 1.0f;
        return matrix3f;
    }
}
