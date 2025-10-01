package dev.vexor.radium.compat.mojang.minecraft.random;

import dev.vexor.radium.compat.mojang.math.Mth;

public class MarsagliaPolarGaussian {
    public final RandomSource randomSource;
    private double nextNextGaussian;
    private boolean haveNextNextGaussian;

    public MarsagliaPolarGaussian(RandomSource randomSource) {
        this.randomSource = randomSource;
    }

    public void reset() {
        this.haveNextNextGaussian = false;
    }

    public double nextGaussian() {
        double d;
        double d2;
        double d3;
        if (this.haveNextNextGaussian) {
            this.haveNextNextGaussian = false;
            return this.nextNextGaussian;
        }
        do {
            d2 = 2.0 * this.randomSource.nextDouble() - 1.0;
            d = 2.0 * this.randomSource.nextDouble() - 1.0;
        } while ((d3 = Mth.square(d2) + Mth.square(d)) >= 1.0 || d3 == 0.0);
        double d4 = Mth.square(-2.0 * Math.log(d3) / d3);
        this.nextNextGaussian = d * d4;
        this.haveNextNextGaussian = true;
        return d2 * d4;
    }
}
