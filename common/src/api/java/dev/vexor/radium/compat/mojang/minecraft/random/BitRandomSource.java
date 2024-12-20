package dev.vexor.radium.compat.mojang.minecraft.random;

public interface BitRandomSource
        extends RandomSource {
    public static final float FLOAT_MULTIPLIER = 5.9604645E-8f;
    public static final double DOUBLE_MULTIPLIER = (double)1.110223E-16f;

    public int next(int var1);

    @Override
    default public int nextInt() {
        return this.next(32);
    }

    @Override
    default public int nextInt(int n) {
        int n2;
        int n3;
        if (n <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        if ((n & n - 1) == 0) {
            return (int)((long)n * (long)this.next(31) >> 31);
        }
        while ((n3 = this.next(31)) - (n2 = n3 % n) + (n - 1) < 0) {
        }
        return n2;
    }

    @Override
    default public long nextLong() {
        int n = this.next(32);
        int n2 = this.next(32);
        long l = (long)n << 32;
        return l + (long)n2;
    }

    @Override
    default public boolean nextBoolean() {
        return this.next(1) != 0;
    }

    @Override
    default public float nextFloat() {
        return (float)this.next(24) * 5.9604645E-8f;
    }

    @Override
    default public double nextDouble() {
        int n = this.next(26);
        int n2 = this.next(27);
        long l = ((long)n << 27) + (long)n2;
        return (double)l * (double)1.110223E-16f;
    }
}