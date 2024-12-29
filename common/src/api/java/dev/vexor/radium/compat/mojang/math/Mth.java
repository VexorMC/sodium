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
}