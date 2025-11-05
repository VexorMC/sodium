package dev.vexor.radium.compat.mojang.minecraft.math;

public final class QuartPos {
    public static final int BITS = 2;
    public static final int MASK = 3;

    public static int fromBlock(int pos) {
        return pos >> BITS;
    }

    public static int quartLocal(int pos) {
        return pos & MASK;
    }
}