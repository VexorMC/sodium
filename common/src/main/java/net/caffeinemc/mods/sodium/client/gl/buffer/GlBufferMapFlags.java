package net.caffeinemc.mods.sodium.client.gl.buffer;

import net.caffeinemc.mods.sodium.client.gl.util.EnumBit;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL44;

public enum GlBufferMapFlags implements EnumBit {
    READ(GL30.GL_MAP_READ_BIT),
    WRITE(GL30.GL_MAP_WRITE_BIT),
    PERSISTENT(GL44.GL_MAP_PERSISTENT_BIT),
    INVALIDATE_BUFFER(GL30.GL_MAP_INVALIDATE_BUFFER_BIT),
    INVALIDATE_RANGE(GL30.GL_MAP_INVALIDATE_RANGE_BIT),
    EXPLICIT_FLUSH(GL30.GL_MAP_FLUSH_EXPLICIT_BIT),
    COHERENT(GL44.GL_MAP_COHERENT_BIT),
    UNSYNCHRONIZED(GL30.GL_MAP_UNSYNCHRONIZED_BIT);

    private final int bit;

    GlBufferMapFlags(int bit) {
        this.bit = bit;
    }

    @Override
    public int getBits() {
        return this.bit;
    }
}
