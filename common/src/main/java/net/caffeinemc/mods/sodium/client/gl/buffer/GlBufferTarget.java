package net.caffeinemc.mods.sodium.client.gl.buffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL42;

public enum GlBufferTarget {
    ARRAY_BUFFER(GL15.GL_ARRAY_BUFFER, GL15.GL_ARRAY_BUFFER_BINDING),
    ELEMENT_BUFFER(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING),
    COPY_READ_BUFFER(GL31.GL_COPY_READ_BUFFER, GL42.GL_COPY_READ_BUFFER_BINDING),
    COPY_WRITE_BUFFER(GL31.GL_COPY_WRITE_BUFFER, GL42.GL_COPY_WRITE_BUFFER_BINDING);

    public static final GlBufferTarget[] VALUES = GlBufferTarget.values();
    public static final int COUNT = VALUES.length;

    private final int target;
    private final int binding;

    GlBufferTarget(int target, int binding) {
        this.target = target;
        this.binding = binding;
    }

    public int getTargetParameter() {
        return this.target;
    }

    public int getBindingParameter() {
        return this.binding;
    }
}
