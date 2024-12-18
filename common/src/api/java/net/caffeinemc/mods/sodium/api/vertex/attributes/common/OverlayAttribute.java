package net.caffeinemc.mods.sodium.api.vertex.attributes.common;

import dev.lunasa.compat.lwjgl3.MemoryUtil;

public class OverlayAttribute {
    public static void set(long ptr, int overlay) {
        MemoryUtil.memPutInt(ptr + 0, overlay);
    }

    public static int get(long ptr) {
        return MemoryUtil.memGetInt(ptr);
    }
}
