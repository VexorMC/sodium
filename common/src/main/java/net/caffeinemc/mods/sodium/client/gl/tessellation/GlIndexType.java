package net.caffeinemc.mods.sodium.client.gl.tessellation;

import org.lwjgl.opengl.GL11;

public enum GlIndexType {
    UNSIGNED_BYTE(GL11.GL_UNSIGNED_BYTE, 1),
    UNSIGNED_SHORT(GL11.GL_UNSIGNED_SHORT, 2),
    UNSIGNED_INT(GL11.GL_UNSIGNED_INT, 4);

    private final int id;
    private final int stride;

    GlIndexType(int id, int stride) {
        this.id = id;
        this.stride = stride;
    }

    public int getFormatId() {
        return this.id;
    }

    public int getStride() {
        return this.stride;
    }
}
