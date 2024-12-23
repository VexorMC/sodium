package net.caffeinemc.mods.sodium.api.vertex.attributes.common;

import dev.vexor.radium.compat.lwjgl3.MemoryUtil;

public class ColorAttribute {
    public static void set(long ptr, int color) {
        MemoryUtil.memPutInt(ptr + 0, color);
    }

    public static int get(long ptr) {
        return MemoryUtil.memGetInt(ptr);
    }
}
