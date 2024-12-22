package dev.vexor.radium.compat.lwjgl3;

import java.lang.reflect.Field;

public class UnsafeAllocator implements MemoryUtil.MemoryAllocator {
    private static final sun.misc.Unsafe UNSAFE;

    static {
        UNSAFE = getUnsafeInstance();
    }

    @Override
    public long malloc(long size) {
        validateSize(size);
        long address = UNSAFE.allocateMemory(size);
        if (address == 0) {
            throw new OutOfMemoryError("Failed to allocate memory");
        }
        return address;
    }

    @Override
    public long calloc(long num, long size) {
        validateSize(num);
        validateSize(size);
        long totalSize = Math.multiplyExact(num, size);
        long address = malloc(totalSize);
        UNSAFE.setMemory(address, totalSize, (byte) 0);
        return address;
    }

    @Override
    public long realloc(long ptr, long size) {
        validateSize(size);
        if (ptr == 0) {
            return malloc(size);
        }
        long newAddress = UNSAFE.reallocateMemory(ptr, size);
        if (newAddress == 0) {
            throw new OutOfMemoryError("Failed to reallocate memory");
        }
        return newAddress;
    }

    @Override
    public void free(long ptr) {
        if (ptr != 0) {
            UNSAFE.freeMemory(ptr);
        }
    }

    @Override
    public long aligned_alloc(long alignment, long size) {
        validateAlignment(alignment);
        validateSize(size);

        long misalignment = alignment - 1;
        long rawAddress = malloc(size + misalignment + Long.BYTES);
        if (rawAddress == 0) {
            throw new OutOfMemoryError("Failed to allocate aligned memory");
        }

        long alignedAddress = (rawAddress + Long.BYTES + misalignment) & ~misalignment;
        UNSAFE.putLong(alignedAddress - Long.BYTES, rawAddress);

        return alignedAddress;
    }

    @Override
    public void aligned_free(long ptr) {
        if (ptr != 0) {
            long rawAddress = UNSAFE.getLong(ptr - Long.BYTES);
            free(rawAddress);
        }
    }

    private static void validateSize(long size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
    }

    private static void validateAlignment(long alignment) {
        if (alignment <= 0 || (alignment & (alignment - 1)) != 0) {
            throw new IllegalArgumentException("Alignment must be a power of 2");
        }
    }

    private static sun.misc.Unsafe getUnsafeInstance() {
        try {
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (sun.misc.Unsafe) unsafeField.get(null);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Unable to access Unsafe instance", e);
        }
    }
}