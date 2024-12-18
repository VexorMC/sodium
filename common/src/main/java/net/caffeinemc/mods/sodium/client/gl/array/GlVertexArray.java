package net.caffeinemc.mods.sodium.client.gl.array;

import net.caffeinemc.mods.sodium.client.gl.GlObject;
import org.lwjgl.opengl.GL30;

/**
 * Provides Vertex Array functionality on supported platforms.
 */
public class GlVertexArray extends GlObject {
    public static final int NULL_ARRAY_ID = 0;

    public GlVertexArray() {
        this.setHandle(GL30.glGenVertexArrays());
    }
}
