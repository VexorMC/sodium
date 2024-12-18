package net.caffeinemc.mods.sodium.api.vertex.attributes.common;

import dev.lunasa.compat.lwjgl3.MemoryUtil;

public class LightAttribute {
    public static void set(long ptr, int light) {
        MemoryUtil.memPutInt(ptr + 0, light);
    }

    public static int get(long ptr) {
        return MemoryUtil.memGetInt(ptr);
    }
}
