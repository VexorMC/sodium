package dev.vexor.radium.compat.mojang.minecraft;

import net.minecraft.util.math.MathHelper;

public class FastColor {
    public static int as8BitChannel(float f) {
        return MathHelper.floor(f * 255.0f);
    }

    public static class ABGR32 {
        public static int alpha(int n) {
            return n >>> 24;
        }

        public static int red(int n) {
            return n & 0xFF;
        }

        public static int green(int n) {
            return n >> 8 & 0xFF;
        }

        public static int blue(int n) {
            return n >> 16 & 0xFF;
        }

        public static int transparent(int n) {
            return n & 0xFFFFFF;
        }

        public static int opaque(int n) {
            return n | 0xFF000000;
        }

        public static int color(int n, int n2, int n3, int n4) {
            return n << 24 | n2 << 16 | n3 << 8 | n4;
        }

        public static int color(int n, int n2) {
            return n << 24 | n2 & 0xFFFFFF;
        }

        public static int fromArgb32(int n) {
            return n & 0xFF00FF00 | (n & 0xFF0000) >> 16 | (n & 0xFF) << 16;
        }
    }

    public static class ARGB32 {
        public static int alpha(int n) {
            return n >>> 24;
        }

        public static int red(int n) {
            return n >> 16 & 0xFF;
        }

        public static int green(int n) {
            return n >> 8 & 0xFF;
        }

        public static int blue(int n) {
            return n & 0xFF;
        }

        public static int color(int n, int n2, int n3, int n4) {
            return n << 24 | n2 << 16 | n3 << 8 | n4;
        }

        public static int color(int n, int n2, int n3) {
            return ARGB32.color(255, n, n2, n3);
        }

        public static int multiply(int n, int n2) {
            return ARGB32.color(ARGB32.alpha(n) * ARGB32.alpha(n2) / 255, ARGB32.red(n) * ARGB32.red(n2) / 255, ARGB32.green(n) * ARGB32.green(n2) / 255, ARGB32.blue(n) * ARGB32.blue(n2) / 255);
        }

        public static int opaque(int n) {
            return n | 0xFF000000;
        }

        public static int color(int n, int n2) {
            return n << 24 | n2 & 0xFFFFFF;
        }

        public static int colorFromFloat(float f, float f2, float f3, float f4) {
            return ARGB32.color(FastColor.as8BitChannel(f), FastColor.as8BitChannel(f2), FastColor.as8BitChannel(f3), FastColor.as8BitChannel(f4));
        }

        public static int average(int n, int n2) {
            return ARGB32.color((ARGB32.alpha(n) + ARGB32.alpha(n2)) / 2, (ARGB32.red(n) + ARGB32.red(n2)) / 2, (ARGB32.green(n) + ARGB32.green(n2)) / 2, (ARGB32.blue(n) + ARGB32.blue(n2)) / 2);
        }
    }
}