package dev.lunasa.compat.mojang;

import net.minecraft.util.math.Vec3d;

import java.util.function.Consumer;

public class Util {
    public static <T> T make(T t, Consumer<? super T> consumer) {
        consumer.accept(t);
        return t;
    }

    public static Vec3d scale(Vec3d self, double d) {
        return new Vec3d(self.x * d, self.y * d, self.z * d);
    }

    public static Vec3d fromRGB24(int n) {
        double d = (double)(n >> 16 & 0xFF) / 255.0;
        double d2 = (double)(n >> 8 & 0xFF) / 255.0;
        double d3 = (double)(n & 0xFF) / 255.0;
        return new Vec3d(d, d2, d3);
    }
}
