package dev.vexor.radium.compat.mojang.minecraft.render;

public class LightTexture {
    public static final int FULL_BRIGHT = 0xF000F0;
    public static final int FULL_SKY = 0xF00000;
    public static final int FULL_BLOCK = 240;

    public static int pack(int n, int n2) {
        return n << 4 | n2 << 20;
    }

    public static int block(int n) {
        return n >> 4 & 0xFFFF;
    }

    public static int sky(int n) {
        return n >> 20 & 0xFFFF;
    }
}