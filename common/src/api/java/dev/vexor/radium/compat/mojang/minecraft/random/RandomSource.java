package dev.vexor.radium.compat.mojang.minecraft.random;

import java.util.Random;

public interface RandomSource {
    @Deprecated
    public static final double GAUSSIAN_SPREAD_FACTOR = 2.297;
    static final Random random = new Random();

    public static RandomSource createNewThreadLocalInstance() {
        return new SingleThreadedRandomSource(random.nextLong());
    }

    public void setSeed(long var1);

    public int nextInt();

    public int nextInt(int var1);

    default public int nextIntBetweenInclusive(int n, int n2) {
        return this.nextInt(n2 - n + 1) + n;
    }

    public long nextLong();

    public boolean nextBoolean();

    public float nextFloat();

    public double nextDouble();

    public double nextGaussian();

    default public double triangle(double d, double d2) {
        return d + d2 * (this.nextDouble() - this.nextDouble());
    }

    default public void consumeCount(int n) {
        for (int i = 0; i < n; ++i) {
            this.nextInt();
        }
    }

    default public int nextInt(int n, int n2) {
        if (n >= n2) {
            throw new IllegalArgumentException("bound - origin is non positive");
        }
        return n + this.nextInt(n2 - n);
    }
}
