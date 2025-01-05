package dev.vexor.radium.compat.mojang.math;

public class Mth {
    public static float square(float f) {
        return f * f;
    }

    public static boolean equal(float f, float f2) {
        return java.lang.Math.abs(f2 - f) < 1.0E-5f;
    }

    public static int roundToward(int n, int n2) {
        return Mth.positiveCeilDiv(n, n2) * n2;
    }

    public static int positiveCeilDiv(int n, int n2) {
        return -java.lang.Math.floorDiv(-n, n2);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static float fastInvCubeRoot(float f) {
        int n = Float.floatToIntBits(f);
        n = 1419967116 - n / 3;
        float f2 = Float.intBitsToFloat(n);
        f2 = 0.6666667f * f2 + 1.0f / (3.0f * f2 * f2 * f);
        f2 = 0.6666667f * f2 + 1.0f / (3.0f * f2 * f2 * f);
        return f2;
    }

}