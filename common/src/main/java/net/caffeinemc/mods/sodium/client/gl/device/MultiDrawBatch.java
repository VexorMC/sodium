package net.caffeinemc.mods.sodium.client.gl.device;

import dev.lunasa.compat.lwjgl3.MemoryUtil;
import dev.lunasa.compat.lwjgl3.Pointer;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * Provides a fixed-size queue for building a draw-command list usable with
 * {@link org.lwjgl.opengl.GL32#glDrawElementsBaseVertex)}.
 */
public final class MultiDrawBatch {
    public final LongBuffer elementPointers;
    public final IntBuffer elementCounts;
    public final IntBuffer baseVertices;

    private final int capacity;

    public int size;

    public MultiDrawBatch(int capacity) {
        this.elementPointers = BufferUtils.createLongBuffer(capacity);
        this.elementCounts = BufferUtils.createIntBuffer(capacity);
        this.baseVertices = BufferUtils.createIntBuffer(capacity);
        this.capacity = capacity;
        this.size = 0;
    }

    public int size() {
        return this.size;
    }

    public int capacity() {
        return this.capacity;
    }

    public void clear() {
        this.size = 0;
    }

    public void delete() {
    }

    public boolean isEmpty() {
        return this.size <= 0;
    }

    public int getIndexBufferSize() {
        int elements = 0;
        for (int i = 0; i < this.size; i++) {
            elements = Math.max(elements, this.elementCounts.get(i));
        }
        return elements;
    }
}
