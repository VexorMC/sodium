package net.caffeinemc.mods.sodium.client.gl.device;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * Provides a fixed-size queue for building a draw-command list usable with
 * {@link org.lwjgl.opengl.GL32#glDrawElementsBaseVertex)}.
 */
public final class MultiDrawBatch {
    public LongBuffer elementPointers;
    public IntBuffer elementCounts;
    public IntBuffer baseVertices;

    private final int capacity;

    public int size;

    public MultiDrawBatch(int capacity) {
        this.capacity = capacity;

        this.elementPointers = BufferUtils.createLongBuffer(capacity);
        this.elementCounts = BufferUtils.createIntBuffer(capacity);
        this.baseVertices = BufferUtils.createIntBuffer(capacity);

        this.elementPointers.clear().limit(capacity).put(new long[capacity]).flip();
        this.elementCounts.clear().limit(capacity).put(new int[capacity]).flip();
        this.baseVertices.clear().limit(capacity).put(new int[capacity]).flip();
    }

    public int size() {
        return this.size;
    }

    public int capacity() {
        return this.capacity;
    }

    public void clear() {
        this.size = 0;

        this.elementPointers.clear().limit(capacity).put(new long[capacity]).flip();
        this.elementCounts.clear().limit(capacity).put(new int[capacity]).flip();
        this.baseVertices.clear().limit(capacity).put(new int[capacity]).flip();
    }

    public void delete() {
        this.elementPointers = null;
        this.elementCounts = null;
        this.baseVertices = null;
    }

    public boolean isEmpty() {
        return this.size <= 0;
    }

    public int getIndexBufferSize() {
        int elements = 0;

        for (int index = 0; index < this.size; index++) {
            elements = Math.max(elements, this.elementCounts.get(index));
        }

        return elements;
    }
}
