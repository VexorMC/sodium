package net.caffeinemc.mods.sodium.api.vertex.format.common;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.*;

public final class EntityVertex {
    public static final VertexFormat FORMAT = VertexFormats.ENTITY;

    public static final int STRIDE = 24;
    public static final int OFFSET_POSITION = 0;
    public static final int OFFSET_TEXTURE = 12;
    public static final int OFFSET_NORMAL = 20;

    public static void write(long ptr,
                             float x, float y, float z, float u, float v,  int normal) {
        PositionAttribute.put(ptr + OFFSET_POSITION, x, y, z);
        TextureAttribute.put(ptr + OFFSET_TEXTURE, u, v);
        NormalAttribute.set(ptr + OFFSET_NORMAL, normal);
    }
}
