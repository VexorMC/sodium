package dev.lunasa.compat.lwjgl3;

public class UnsafeAllocator implements MemoryUtil.MemoryAllocator {
    static final sun.misc.Unsafe UNSAFE;

    static {
        UNSAFE = getUnsafeInstance();
    }

    @Override
    public long malloc(long size) {
        return UNSAFE.allocateMemory(size);
    }

    @Override
    public long calloc(long num, long size) {
        long totalSize = num * size;
        long address = UNSAFE.allocateMemory(totalSize);
        if (address == 0) {
            throw new OutOfMemoryError("Failed to allocate memory");
        }
        UNSAFE.setMemory(address, totalSize, (byte) 0);
        return address;
    }

    @Override
    public long realloc(long ptr, long size) {
        if (ptr == 0) {
            return malloc(size);
        }
        return UNSAFE.reallocateMemory(ptr, size);
    }

    @Override
    public void free(long ptr) {
        if (ptr != 0) {
            UNSAFE.freeMemory(ptr);
        }
    }

    @Override
    public long aligned_alloc(long alignment, long size) {
        long misalignment = alignment - 1;
        long address = malloc(size + misalignment);
        if (address == 0) {
            throw new OutOfMemoryError("Failed to allocate aligned memory");
        }
        long alignedAddress = (address + misalignment) & ~misalignment;
        return alignedAddress;
    }

    @Override
    public void aligned_free(long ptr) {
        free(ptr);
    }

    public static sun.misc.Unsafe getUnsafeInstance() throws SecurityException {
        java.lang.reflect.Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (!field.getType().equals(sun.misc.Unsafe.class))
                continue;
            int modifiers = field.getModifiers();
            if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers)))
                continue;
            field.setAccessible(true);
            try {
                return (sun.misc.Unsafe) field.get(null);
            } catch (IllegalAccessException e) {
                /* Ignore */
            }
            break;
        }
        throw new UnsupportedOperationException("Unable to access Unsafe instance");
    }
}