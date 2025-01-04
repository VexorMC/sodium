package dev.vexor.radium.compat.mojang;

import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> biFunction) {
        return new BiFunction<T, U, R>(){
            private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T t, U u) {
                return this.cache.computeIfAbsent(new Pair<>(t, u), pair -> biFunction.apply(pair.getLeft(), pair.getRight()));
            }

            public String toString() {
                return "memoize/2[function=" + String.valueOf(biFunction) + ", size=" + this.cache.size() + "]";
            }
        };
    }


}
