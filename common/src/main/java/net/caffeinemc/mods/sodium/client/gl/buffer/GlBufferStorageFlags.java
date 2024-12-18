package net.caffeinemc.mods.sodium.client.gl.buffer;

import net.caffeinemc.mods.sodium.client.gl.util.EnumBit;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL44;

public enum GlBufferStorageFlags implements EnumBit {
    PERSISTENT(GL44.GL_MAP_PERSISTENT_BIT),
    MAP_READ(GL30.GL_MAP_READ_BIT),
    MAP_WRITE(GL30.GL_MAP_WRITE_BIT),
    CLIENT_STORAGE(GL44.GL_CLIENT_STORAGE_BIT),
    COHERENT(GL44.GL_MAP_COHERENT_BIT);

    private final int bits;

    GlBufferStorageFlags(int bits) {
        this.bits = bits;
    }

    @Override
    public int getBits() {
        return this.bits;
    }
}
