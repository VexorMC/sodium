package dev.lunasa.compat.lwjgl3;

public class UnsafeAllocator implements MemoryUtil.MemoryAllocator {
    @Override
    public long getMalloc() {
        return 0;
    }

    @Override
    public long getCalloc() {
        return 0;
    }

    @Override
    public long getRealloc() {
        return 0;
    }

    @Override
    public long getFree() {
        return 0;
    }

    @Override
    public long getAlignedAlloc() {
        return 0;
    }

    @Override
    public long getAlignedFree() {
        return 0;
    }

    @Override
    public long malloc(long size) {
        return 0;
    }

    @Override
    public long calloc(long num, long size) {
        return 0;
    }

    @Override
    public long realloc(long ptr, long size) {
        return 0;
    }

    @Override
    public void free(long ptr) {

    }

    @Override
    public long aligned_alloc(long alignment, long size) {
        return 0;
    }

    @Override
    public void aligned_free(long ptr) {

    }
}
