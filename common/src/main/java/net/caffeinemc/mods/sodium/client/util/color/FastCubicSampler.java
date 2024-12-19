package net.caffeinemc.mods.sodium.client.util.color;

import dev.lunasa.compat.mojang.Util;
import dev.lunasa.compat.mojang.math.Mth;
import net.minecraft.util.math.Vec3d;

import java.util.function.Function;

public class FastCubicSampler {
    private static final double[] DENSITY_CURVE = new double[] { 0.0D, 1.0D, 4.0D, 6.0D, 4.0D, 1.0D, 0.0D };
    private static final int DIAMETER = 6;

    public static Vec3d sampleColor(Vec3d pos, ColorFetcher colorFetcher, Function<Vec3d, Vec3d> transformer) {
        int intX = Mth.floor(pos.x);
        int intY = Mth.floor(pos.y);
        int intZ = Mth.floor(pos.z);

        int[] values = new int[DIAMETER * DIAMETER * DIAMETER];

        for(int x = 0; x < DIAMETER; ++x) {
            int blockX = (intX - 2) + x;

            for(int y = 0; y < DIAMETER; ++y) {
                int blockY = (intY - 2) + y;

                for(int z = 0; z < DIAMETER; ++z) {
                    int blockZ = (intZ - 2) + z;

                    values[index(x, y, z)] = colorFetcher.fetch(blockX, blockY, blockZ);
                }
            }
        }

        // Fast path! Skip blending the colors if all inputs are the same
        if (isHomogenousArray(values)) {
            // Take the first color if it's homogenous (all elements are the same...)
            return transformer.apply(Util.fromRGB24(values[0]));
        }

        double deltaX = pos.x - (double)intX;
        double deltaY = pos.y - (double)intY;
        double deltaZ = pos.z - (double)intZ;

        Vec3d sum = new Vec3d(0, 0, 0);
        double totalFactor = 0.0D;

        for(int x = 0; x < DIAMETER; ++x) {
            double densityX = Mth.lerp(deltaX, DENSITY_CURVE[x + 1], DENSITY_CURVE[x]);

            for(int y = 0; y < DIAMETER; ++y) {
                double densityY = Mth.lerp(deltaY, DENSITY_CURVE[y + 1], DENSITY_CURVE[y]);

                for(int z = 0; z < DIAMETER; ++z) {
                    double densityZ = Mth.lerp(deltaZ, DENSITY_CURVE[z + 1], DENSITY_CURVE[z]);

                    double factor = densityX * densityY * densityZ;
                    totalFactor += factor;

                    Vec3d color = transformer.apply(Util.fromRGB24(values[index(x, y, z)]));
                    sum = sum.add(new Vec3d(color.x * factor, color.y * factor, color.z * factor));
                }
            }
        }
        sum = new Vec3d(sum.x * (1.0D / totalFactor), sum.y * (1.0D / totalFactor), sum.z * (1.0D / totalFactor));

        return sum;
    }

    private static int index(int x, int y, int z) {
        return (DIAMETER * DIAMETER * z) + (DIAMETER * y) + x;
    }

    public interface ColorFetcher {
        int fetch(int x, int y, int z);
    }

    private static boolean isHomogenousArray(int[] arr) {
        int val = arr[0];

        for (int i = 1; i < arr.length; i++) {
            if (arr[i] != val) {
                return false;
            }
        }

        return true;
    }
}
