package dev.vexor.radium.compat.mojang.minecraft.math;

public final class QuartPos {
    public static final int BITS = 2;
    public static final int SIZE = 4;
    public static final int MASK = 3;
    private static final int SECTION_TO_QUARTS_BITS = 2;

    private QuartPos() {
    }

    public static int fromBlock(int n) {
        return n >> 2;
    }

    public static int quartLocal(int n) {
        return n & 3;
    }

    public static int toBlock(int n) {
        return n << 2;
    }

    public static int fromSection(int n) {
        return n << 2;
    }

    public static int toSection(int n) {
        return n >> 2;
    }
}