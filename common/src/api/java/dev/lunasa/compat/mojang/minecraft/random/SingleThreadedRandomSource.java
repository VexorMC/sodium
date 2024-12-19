package dev.lunasa.compat.mojang.minecraft.random;

public class SingleThreadedRandomSource
        implements BitRandomSource {
    private static final int MODULUS_BITS = 48;
    private static final long MODULUS_MASK = 0xFFFFFFFFFFFFL;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private long seed;
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public SingleThreadedRandomSource(long l) {
        this.setSeed(l);
    }

    @Override
    public void setSeed(long l) {
        this.seed = (l ^ 0x5DEECE66DL) & 0xFFFFFFFFFFFFL;
        this.gaussianSource.reset();
    }

    @Override
    public int next(int n) {
        long l;
        this.seed = l = this.seed * 25214903917L + 11L & 0xFFFFFFFFFFFFL;
        return (int)(l >> 48 - n);
    }

    @Override
    public double nextGaussian() {
        return this.gaussianSource.nextGaussian();
    }
}