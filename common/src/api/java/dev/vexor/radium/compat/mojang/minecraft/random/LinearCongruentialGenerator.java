package dev.vexor.radium.compat.mojang.minecraft.random;

public class LinearCongruentialGenerator {
    public static long next(long l, long l2) {
        l *= l * 6364136223846793005L + 1442695040888963407L;
        return l += l2;
    }
}