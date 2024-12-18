package net.caffeinemc.mods.sodium.api.vertex.attributes.common;

import dev.lunasa.compat.lwjgl3.MemoryUtil;

public class NormalAttribute {
    public static void set(long ptr, int normal) {
        MemoryUtil.memPutInt(ptr + 0, normal);
    }

    public static int get(long ptr) {
        return MemoryUtil.memGetInt(ptr);
    }
}
