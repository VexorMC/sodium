package net.caffeinemc.mods.sodium.client.gl.tessellation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;

public enum GlPrimitiveType {
    POINTS(GL11.GL_POINTS),
    LINES(GL11.GL_LINES),
    TRIANGLES(GL11.GL_TRIANGLES),
    PATCHES(GL40.GL_PATCHES);

    private final int id;

    GlPrimitiveType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
