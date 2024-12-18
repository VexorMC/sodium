package dev.lunasa.compat.mojang.math;

import net.minecraft.util.math.Vec3d;

public class Mth {
    public static float square(float f) {
        return f * f;
    }

    public static double square(double d) {
        return d * d;
    }

    public static int square(int n) {
        return n * n;
    }

    public static long square(long l) {
        return l * l;
    }

    public static boolean equal(float f, float f2) {
        return java.lang.Math.abs(f2 - f) < 1.0E-5f;
    }

    public static boolean equal(double d, double d2) {
        return java.lang.Math.abs(d2 - d) < (double)1.0E-5f;
    }

    public static int roundToward(int n, int n2) {
        return Mth.positiveCeilDiv(n, n2) * n2;
    }

    public static int positiveCeilDiv(int n, int n2) {
        return -java.lang.Math.floorDiv(-n, n2);
    }

    public static int floor(float f) {
        int n = (int)f;
        return f < (float)n ? n - 1 : n;
    }

    public static float lerp(float f, float f2, float f3) {
        return f2 + f * (f3 - f2);
    }

    public static double lerp(double d, double d2, double d3) {
        return d2 + d * (d3 - d2);
    }

    public static int floor(double d) {
        int n = (int)d;
        return d < (double)n ? n - 1 : n;
    }

    public Vec3d lerp(Vec3d self, Vec3d vec3, double d) {
        return new Vec3d(Mth.lerp(d, self.x, vec3.x), Mth.lerp(d, self.y, vec3.y), Mth.lerp(d, self.z, vec3.z));
    }

    public static int smallestEncompassingPowerOfTwo(int n) {
        int n2 = n - 1;
        n2 |= n2 >> 1;
        n2 |= n2 >> 2;
        n2 |= n2 >> 4;
        n2 |= n2 >> 8;
        n2 |= n2 >> 16;
        return n2 + 1;
    }

    public static boolean isPowerOfTwo(int n) {
        return n != 0 && (n & n - 1) == 0;
    }

    public static int ceillog2(int n) {
        n = Mth.isPowerOfTwo(n) ? n : Mth.smallestEncompassingPowerOfTwo(n);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)n * 125613361L >> 27) & 0x1F];
    }

    public static int murmurHash3Mixer(int n) {
        n ^= n >>> 16;
        n *= -2048144789;
        n ^= n >>> 13;
        n *= -1028477387;
        n ^= n >>> 16;
        return n;
    }

    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
}