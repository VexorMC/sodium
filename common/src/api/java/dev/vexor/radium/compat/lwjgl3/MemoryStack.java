package dev.vexor.radium.compat.lwjgl3;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import java.nio.*;
import java.util.Arrays;

public class MemoryStack extends Pointer.Default implements AutoCloseable {

    private static final int DEFAULT_STACK_SIZE = 64 * 1024;
    private static final int DEFAULT_STACK_FRAMES = 8;

    private static final ThreadLocal<MemoryStack> TLS = ThreadLocal.withInitial(MemoryStack::create);

    static {
        if (DEFAULT_STACK_SIZE < 0) {
            throw new IllegalStateException("Invalid stack size.");
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @Nullable
    private final ByteBuffer container;

    private final int size;

    private int pointer;

    private   int[] frames;
    protected int   frameIndex;

    /**
     * Creates a new {@code MemoryStack} backed by the specified memory region.
     *
     * <p>In the initial state, there is no active stack frame. The {@link #push} method must be used before any allocations.</p>
     *
     * @param container the backing memory buffer, may be null
     * @param address   the backing memory address
     * @param size      the backing memory size
     */
    protected MemoryStack(@Nullable ByteBuffer container, long address, int size) {
        super(address);
        this.container = container;

        this.size = size;
        this.pointer = size;

        this.frames = new int[DEFAULT_STACK_FRAMES];
    }

    /**
     * Creates a new {@code MemoryStack} with the default size.
     *
     * <p>In the initial state, there is no active stack frame. The {@link #push} method must be used before any allocations.</p>
     */
    public static MemoryStack create() {
        return create(DEFAULT_STACK_SIZE);
    }

    /**
     * Creates a new {@code MemoryStack} with the specified size.
     *
     * <p>In the initial state, there is no active stack frame. The {@link #push} method must be used before any allocations.</p>
     *
     * @param capacity the maximum number of bytes that may be allocated on the stack
     */
    public static MemoryStack create(int capacity) {
        return create(BufferUtils.createByteBuffer(capacity));
    }

    /**
     * Creates a new {@code MemoryStack} backed by the specified memory buffer.
     *
     * <p>In the initial state, there is no active stack frame. The {@link #push} method must be used before any allocations.</p>
     *
     * @param buffer the backing memory buffer
     */
    public static MemoryStack create(ByteBuffer buffer) {
        long address = MemoryUtil.memAddress(buffer);
        int  size = buffer.remaining();
        return new MemoryStack(buffer, address, size);
    }

    /**
     * Creates a new {@code MemoryStack} backed by the specified memory region.
     *
     * <p>In the initial state, there is no active stack frame. The {@link #push} method must be used before any allocations.</p>
     *
     * @param address the backing memory address
     * @param size    the backing memory size
     */
    public static MemoryStack ncreate(long address, int size) {
        return new MemoryStack(null, address, size);
    }

    /**
     * Stores the current stack pointer and pushes a new frame to the stack.
     *
     * <p>This method should be called when entering a method, before doing any stack allocations. When exiting a method, call the {@link #pop} method to
     * restore the previous stack frame.</p>
     *
     * <p>Pairs of push/pop calls may be nested. Care must be taken to:</p>
     * <ul>
     * <li>match every push with a pop</li>
     * <li>not call pop before push has been called at least once</li>
     * <li>not nest push calls to more than the maximum supported depth</li>
     * </ul>
     *
     * @return this stack
     */
    public MemoryStack push() {
        if (frameIndex == frames.length) {
            frameOverflow();
        }

        frames[frameIndex++] = pointer;
        return this;
    }

    private void frameOverflow() {
        frames = Arrays.copyOf(frames, frames.length * 3 / 2);
    }

    /**
     * Pops the current stack frame and moves the stack pointer to the end of the previous stack frame.
     *
     * @return this stack
     */
    public MemoryStack pop() {
        pointer = frames[--frameIndex];
        return this;
    }

    /**
     * Calls {@link #pop} on this {@code MemoryStack}.
     *
     * <p>This method should not be used directly. It is called automatically when the {@code MemoryStack} is used as a resource in a try-with-resources
     * statement.</p>
     */
    @Override
    public void close() {
        //noinspection resource
        pop();
    }

    /**
     * Returns the address of the backing off-heap memory.
     *
     * <p>The stack grows "downwards", so the bottom of the stack is at {@code address + size}, while the top is at {@code address}.</p>
     */
    public long getAddress() {
        return address;
    }

    /**
     * Returns the size of the backing off-heap memory.
     *
     * <p>This is the maximum number of bytes that may be allocated on the stack.</p>
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the current frame index.
     *
     * <p>This is the current number of nested {@link #push} calls.</p>
     */
    public int getFrameIndex() {
        return frameIndex;
    }

    /** Returns the memory address at the current stack pointer. */
    public long getPointerAddress() {
        return address + (pointer & 0xFFFF_FFFFL);
    }

    /**
     * Returns the current stack pointer.
     *
     * <p>The stack grows "downwards", so when the stack is empty {@code pointer} is equal to {@code size}. On every allocation {@code pointer} is reduced by
     * the allocated size (after alignment) and {@code address + pointer} points to the first byte of the last allocation.</p>
     *
     * <p>Effectively, this methods returns how many more bytes may be allocated on the stack.</p>
     */
    public int getPointer() {
        return pointer;
    }

    /**
     * Sets the current stack pointer.
     *
     * <p>This method directly manipulates the stack pointer. Using it irresponsibly may break the internal state of the stack. It should only be used in rare
     * cases or in auto-generated code.</p>
     */
    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    private void checkPointer(int pointer) {
        if (pointer < 0 || size < pointer) {
            throw new IndexOutOfBoundsException("Invalid stack pointer");
        }
    }

    private static void checkAlignment(int alignment) {
        if (Integer.bitCount(alignment) != 1) {
            throw new IllegalArgumentException("Alignment must be a power-of-two value.");
        }
    }

    /**
     * Calls {@link #nmalloc(int, int)} with {@code alignment} equal to {@link Pointer#POINTER_SIZE POINTER_SIZE}.
     *
     * @param size the allocation size
     *
     * @return the memory address on the stack for the requested allocation
     */
    public long nmalloc(int size) {
        return nmalloc(POINTER_SIZE, size);
    }

    /**
     * Allocates a block of {@code size} bytes of memory on the stack. The content of the newly allocated block of memory is not initialized, remaining with
     * indeterminate values.
     *
     * @param alignment the required alignment
     * @param size      the allocation size
     *
     * @return the memory address on the stack for the requested allocation
     */
    public long nmalloc(int alignment, int size) {
        // Align address to the specified alignment
        long address = (this.address + pointer - size) & ~Integer.toUnsignedLong(alignment - 1);

        pointer = (int)(address - this.address);
        if (pointer < 0) {
            throw new OutOfMemoryError("Out of stack space.");
        }

        return address;
    }

    /**
     * Allocates a block of memory on the stack for an array of {@code num} elements, each of them {@code size} bytes long, and initializes all its bits to
     * zero.
     *
     * @param alignment the required element alignment
     * @param num       num  the number of elements to allocate
     * @param size      the size of each element
     *
     * @return the memory address on the stack for the requested allocation
     */
    public long ncalloc(int alignment, int num, int size) {
        int  bytes = num * size;
        long address = nmalloc(alignment, bytes);
        MemoryUtil.memSet(address, 0, bytes);
        return address;
    }

    // -------------------------------------------------

    /**
     * Allocates an aligned {@link ByteBuffer} on the stack.
     *
     * @param alignment the required buffer alignment
     * @param size      the number of elements in the buffer
     *
     * @return the allocated buffer
     */
    public ByteBuffer malloc(int alignment, int size) {
        return MemoryUtil.wrapBufferByte(nmalloc(alignment, size), size).order(MemoryUtil.NATIVE_ORDER);
    }

    /** Calloc version of {@link #malloc(int, int)}. */
    public ByteBuffer calloc(int alignment, int size) {
        return MemoryUtil.wrapBufferByte(ncalloc(alignment, size, 1), size).order(MemoryUtil.NATIVE_ORDER);
    }

    /**
     * Allocates a {@link ByteBuffer} on the stack with {@code alignment} equal to {@link Pointer#POINTER_SIZE POINTER_SIZE}.
     *
     * @param size the number of elements in the buffer
     *
     * @return the allocated buffer
     */
    public ByteBuffer malloc(int size) {
        return MemoryUtil.wrapBufferByte(nmalloc(POINTER_SIZE, size), size).order(MemoryUtil.NATIVE_ORDER);
    }
    /** Calloc version of {@link #malloc(int)}. */
    public ByteBuffer calloc(int size) {
        return MemoryUtil.wrapBufferByte(ncalloc(POINTER_SIZE, size, 1), size).order(MemoryUtil.NATIVE_ORDER);
    }

    /** Unsafe version of {@link #bytes(byte)}. */
    public long nbyte(byte value) {
        long a = nmalloc(1, 1);
        MemoryUtil.memPutByte(a, value);
        return a;
    }
    /** Single value version of {@link #malloc}. */
    public ByteBuffer bytes(byte x) { return malloc(1, 1).put(0, x); }
    /** Two value version of {@link #malloc}. */
    public ByteBuffer bytes(byte x, byte y) { return malloc(1, 2).put(0, x).put(1, y); }
    /** Three value version of {@link #malloc}. */
    public ByteBuffer bytes(byte x, byte y, byte z) { return malloc(1, 3).put(0, x).put(1, y).put(2, z); }
    /** Four value version of {@link #malloc}. */
    public ByteBuffer bytes(byte x, byte y, byte z, byte w) { return malloc(1, 4).put(0, x).put(1, y).put(2, z).put(3, w); }
    /** Vararg version of {@link #malloc}. */
    public ByteBuffer bytes(byte... values) {
        ByteBuffer buffer = malloc(1, values.length).put(values);
        buffer.flip();
        return buffer;
    }

    // -------------------------------------------------

    /** Short version of {@link #malloc(int)}. */
    public ShortBuffer mallocShort(int size) { return MemoryUtil.wrapBufferShort( nmalloc(2, size << 1), size); }
    /** Short version of {@link #calloc(int)}. */
    public ShortBuffer callocShort(int size) {
        int  bytes   = size * 2;
        long address = nmalloc(2, bytes);
        MemoryUtil.memSet(address, 0, bytes);
        return MemoryUtil.wrapBufferShort( address, size);
    }

    /** Unsafe version of {@link #shorts(short)}. */
    public long nshort(short value) {
        long a = nmalloc(2, 2);
        MemoryUtil.memPutShort(a, value);
        return a;
    }
    /** Single value version of {@link #mallocShort}. */
    public ShortBuffer shorts(short x) { return mallocShort(1).put(0, x); }
    /** Two value version of {@link #mallocShort}. */
    public ShortBuffer shorts(short x, short y) { return mallocShort(2).put(0, x).put(1, y); }
    /** Three value version of {@link #mallocShort}. */
    public ShortBuffer shorts(short x, short y, short z) { return mallocShort(3).put(0, x).put(1, y).put(2, z); }
    /** Four value version of {@link #mallocShort}. */
    public ShortBuffer shorts(short x, short y, short z, short w) { return mallocShort(4).put(0, x).put(1, y).put(2, z).put(3, w); }
    /** Vararg version of {@link #mallocShort}. */
    public ShortBuffer shorts(short... values) {
        ShortBuffer buffer = mallocShort(values.length).put(values);
        buffer.flip();
        return buffer;
    }

    // -------------------------------------------------

    /** Int version of {@link #malloc(int)}. */
    public IntBuffer mallocInt(int size) { return MemoryUtil.wrapBufferInt( nmalloc(4, size << 2), size); }
    /** Int version of {@link #calloc(int)}. */
    public IntBuffer callocInt(int size) {
        int  bytes   = size * 4;
        long address = nmalloc(4, bytes);
        MemoryUtil.memSet(address, 0, bytes);
        return MemoryUtil.wrapBufferInt( address, size);
    }

    /** Unsafe version of {@link #ints(int)}. */
    public long nint(int value) {
        long a = nmalloc(4, 4);
        MemoryUtil.memPutInt(a, value);
        return a;
    }
    /** Single value version of {@link #mallocInt}. */
    public IntBuffer ints(int x) { return mallocInt(1).put(0, x); }
    /** Two value version of {@link #mallocInt}. */
    public IntBuffer ints(int x, int y) { return mallocInt(2).put(0, x).put(1, y); }
    /** Three value version of {@link #mallocInt}. */
    public IntBuffer ints(int x, int y, int z) { return mallocInt(3).put(0, x).put(1, y).put(2, z); }
    /** Four value version of {@link #mallocInt}. */
    public IntBuffer ints(int x, int y, int z, int w) { return mallocInt(4).put(0, x).put(1, y).put(2, z).put(3, w); }
    /** Vararg version of {@link #mallocInt}. */
    public IntBuffer ints(int... values) {
        IntBuffer buffer = mallocInt(values.length).put(values);
        buffer.flip();
        return buffer;
    }

    // -------------------------------------------------

    /** Long version of {@link #malloc(int)}. */
    public LongBuffer mallocLong(int size) { return MemoryUtil.wrapBufferLong( nmalloc(8, size << 3), size); }
    /** Long version of {@link #calloc(int)}. */
    public LongBuffer callocLong(int size) {
        int  bytes   = size * 8;
        long address = nmalloc(8, bytes);
        MemoryUtil.memSet(address, 0, bytes);
        return MemoryUtil.wrapBufferLong( address, size);
    }

    /** Unsafe version of {@link #longs(long)}. */
    public long nlong(long value) {
        long a = nmalloc(8, 8);
        MemoryUtil.memPutLong(a, value);
        return a;
    }
    /** Single value version of {@link #mallocLong}. */
    public LongBuffer longs(long x) { return mallocLong(1).put(0, x); }
    /** Two value version of {@link #mallocLong}. */
    public LongBuffer longs(long x, long y) { return mallocLong(2).put(0, x).put(1, y); }
    /** Three value version of {@link #mallocLong}. */
    public LongBuffer longs(long x, long y, long z) { return mallocLong(3).put(0, x).put(1, y).put(2, z); }
    /** Four value version of {@link #mallocLong}. */
    public LongBuffer longs(long x, long y, long z, long w) { return mallocLong(4).put(0, x).put(1, y).put(2, z).put(3, w); }
    /** Vararg version of {@link #mallocLong}. */
    public LongBuffer longs(long... more) {
        LongBuffer buffer = mallocLong(more.length).put(more);
        buffer.flip();
        return buffer;
    }

    // -------------------------------------------------

    /** Float version of {@link #malloc(int)}. */
    public FloatBuffer mallocFloat(int size) { return MemoryUtil.wrapBufferFloat( nmalloc(4, size << 2), size); }
    /** Float version of {@link #calloc(int)}. */
    public FloatBuffer callocFloat(int size) {
        int  bytes   = size * 4;
        long address = nmalloc(4, bytes);
        MemoryUtil.memSet(address, 0, bytes);
        return MemoryUtil.wrapBufferFloat( address, size);
    }

    /** Unsafe version of {@link #floats(float)}. */
    public long nfloat(float value) {
        long a = nmalloc(4, 4);
        MemoryUtil.memPutFloat(a, value);
        return a;
    }
    /** Single value version of {@link #mallocFloat}. */
    public FloatBuffer floats(float x) { return mallocFloat(1).put(0, x); }
    /** Two value version of {@link #mallocFloat}. */
    public FloatBuffer floats(float x, float y) { return mallocFloat(2).put(0, x).put(1, y); }
    /** Three value version of {@link #mallocFloat}. */
    public FloatBuffer floats(float x, float y, float z) { return mallocFloat(3).put(0, x).put(1, y).put(2, z); }
    /** Four value version of {@link #mallocFloat}. */
    public FloatBuffer floats(float x, float y, float z, float w) { return mallocFloat(4).put(0, x).put(1, y).put(2, z).put(3, w); }
    /** Vararg version of {@link #mallocFloat}. */
    public FloatBuffer floats(float... values) {
        FloatBuffer buffer = mallocFloat(values.length).put(values);
        buffer.flip();
        return buffer;
    }

    // -------------------------------------------------

    /** Double version of {@link #malloc(int)}. */
    public DoubleBuffer mallocDouble(int size) { return MemoryUtil.wrapBufferDouble( nmalloc(8, size << 3), size); }
    /** Double version of {@link #calloc(int)}. */
    public DoubleBuffer callocDouble(int size) {
        int  bytes   = size * 8;
        long address = nmalloc(8, bytes);
        MemoryUtil.memSet(address, 0, bytes);
        return MemoryUtil.wrapBufferDouble( address, size);
    }

    /** Unsafe version of {@link #doubles(double)}. */
    public long ndouble(double value) {
        long a = nmalloc(8, 8);
        MemoryUtil.memPutDouble(a, value);
        return a;
    }
    /** Single value version of {@link #mallocDouble}. */
    public DoubleBuffer doubles(double x) { return mallocDouble(1).put(0, x); }
    /** Two value version of {@link #mallocDouble}. */
    public DoubleBuffer doubles(double x, double y) { return mallocDouble(2).put(0, x).put(1, y); }
    /** Three value version of {@link #mallocDouble}. */
    public DoubleBuffer doubles(double x, double y, double z) { return mallocDouble(3).put(0, x).put(1, y).put(2, z); }
    /** Four value version of {@link #mallocDouble}. */
    public DoubleBuffer doubles(double x, double y, double z, double w) { return mallocDouble(4).put(0, x).put(1, y).put(2, z).put(3, w); }
    /** Vararg version of {@link #mallocDouble}. */
    public DoubleBuffer doubles(double... values) {
        DoubleBuffer buffer = mallocDouble(values.length).put(values);
        buffer.flip();
        return buffer;
    }

    public long npointer(long value) {
        long a = nmalloc(POINTER_SIZE, POINTER_SIZE);
        MemoryUtil.memPutAddress(a, value);
        return a;
    }

    public long npointer(Pointer value) {
        long a = nmalloc(POINTER_SIZE, POINTER_SIZE);
        MemoryUtil.memPutAddress(a, value.address());
        return a;
    }

    public long npointer(Buffer value) {
        long a = nmalloc(POINTER_SIZE, POINTER_SIZE);
        MemoryUtil.memPutAddress(a, MemoryUtil.memAddress(value));
        return a;
    }

    // -------------------------------------------------

    /**
     * Encodes the specified text on the stack using ASCII encoding and returns a {@code ByteBuffer} that points to the encoded text, including a
     * null-terminator.
     *
     * <p>The buffer will have {@code alignment} equal to {@link Pointer#POINTER_SIZE POINTER_SIZE}.</p>
     *
     * @param text the text to encode
     */
    public ByteBuffer ASCII(CharSequence text) {
        return ASCII(text, true);
    }

    /**
     * Encodes the specified text on the stack using ASCII encoding and returns a {@code ByteBuffer} that points to the encoded text.
     *
     * <p>The buffer will have {@code alignment} equal to {@link Pointer#POINTER_SIZE POINTER_SIZE}.</p>
     *
     * @param text           the text to encode
     * @param nullTerminated if true, a null-terminator is included at the end of the encoded text
     */
    public ByteBuffer ASCII(CharSequence text, boolean nullTerminated) {
        int  length = MemoryUtil.memLengthASCII(text, nullTerminated);
        long target = nmalloc(POINTER_SIZE, length);
        MemoryUtil.encodeASCIIUnsafe(text, nullTerminated, target);
        return MemoryUtil.wrapBufferByte(target, length).order(MemoryUtil.NATIVE_ORDER);
    }

    /**
     * Encodes the specified text on the stack using ASCII encoding and returns the encoded text length, in bytes.
     *
     * <p>Use {@link #getPointerAddress} immediately after this method to get the encoded text address, which will have {@code alignment} equal to
     * {@link Pointer#POINTER_SIZE POINTER_SIZE}.</p>
     *
     * @param text           the text to encode
     * @param nullTerminated if true, a null-terminator is included at the end of the encoded text
     */
    public int nASCII(CharSequence text, boolean nullTerminated) {
        long target = nmalloc(POINTER_SIZE, MemoryUtil.memLengthASCII(text, nullTerminated));
        return MemoryUtil.encodeASCIIUnsafe(text, nullTerminated, target);
    }

    /** Like {@link #ASCII(CharSequence) ASCII}, but returns {@code null} if {@code text} is {@code null}. */
    @Nullable
    public ByteBuffer ASCIISafe(@Nullable CharSequence text) {
        return ASCIISafe(text, true);
    }

    /** Like {@link #ASCII(CharSequence, boolean) ASCII}, but returns {@code null} if {@code text} is {@code null}. */
    @Nullable
    public ByteBuffer ASCIISafe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? null : ASCII(text, nullTerminated);
    }

    /** Like {@link #nASCII(CharSequence, boolean) nASCII}, but returns 0 if {@code text} is {@code null}. */
    public int nASCIISafe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? 0 : nASCII(text, nullTerminated);
    }

    /**
     * Encodes the specified text on the stack using UTF8 encoding and returns a {@code ByteBuffer} that points to the encoded text, including a
     * null-terminator.
     *
     * <p>The buffer will have {@code alignment} equal to {@link Pointer#POINTER_SIZE POINTER_SIZE}.</p>
     *
     * @param text the text to encode
     */
    public ByteBuffer UTF8(CharSequence text) {
        return UTF8(text, true);
    }

    /**
     * Encodes the specified text on the stack using UTF8 encoding and returns a {@code ByteBuffer} that points to the encoded text.
     *
     * <p>The buffer will have {@code alignment} equal to {@link Pointer#POINTER_SIZE POINTER_SIZE}.</p>
     *
     * @param text           the text to encode
     * @param nullTerminated if true, a null-terminator is included at the end of the encoded text
     */
    public ByteBuffer UTF8(CharSequence text, boolean nullTerminated) {
        int  length = MemoryUtil.memLengthUTF8(text, nullTerminated);
        long target = nmalloc(POINTER_SIZE, length);
        MemoryUtil.encodeUTF8Unsafe(text, nullTerminated, target);
        return MemoryUtil.wrapBufferByte(target, length).order(MemoryUtil.NATIVE_ORDER);
    }

    /**
     * Encodes the specified text on the stack using UTF8 encoding and returns the encoded text length, in bytes.
     *
     * <p>Use {@link #getPointerAddress} immediately after this method to get the encoded text address, which will have {@code alignment} equal to
     * {@link Pointer#POINTER_SIZE POINTER_SIZE}.</p>
     *
     * @param text           the text to encode
     * @param nullTerminated if true, a null-terminator is included at the end of the encoded text
     */
    public int nUTF8(CharSequence text, boolean nullTerminated) {
        long target = nmalloc(POINTER_SIZE, MemoryUtil.memLengthUTF8(text, nullTerminated));
        return MemoryUtil.encodeUTF8Unsafe(text, nullTerminated, target);
    }

    /** Like {@link #UTF8(CharSequence) UTF8}, but returns {@code null} if {@code text} is {@code null}. */
    @Nullable
    public ByteBuffer UTF8Safe(@Nullable CharSequence text) {
        return UTF8Safe(text, true);
    }

    /** Like {@link #UTF8(CharSequence, boolean) UTF8}, but returns {@code null} if {@code text} is {@code null}. */
    @Nullable
    public ByteBuffer UTF8Safe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? null : UTF8(text, nullTerminated);
    }

    /** Like {@link #nUTF8(CharSequence, boolean) nUTF8}, but returns 0 if {@code text} is {@code null}. */
    public int nUTF8Safe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? 0 : nUTF8(text, nullTerminated);
    }

    /**
     * Encodes the specified text on the stack using UTF16 encoding and returns a {@code ByteBuffer} that points to the encoded text, including a
     * null-terminator.
     *
     * <p>The buffer will have {@code alignment} equal to {@link Pointer#POINTER_SIZE POINTER_SIZE}.</p>
     *
     * @param text the text to encode
     */
    public ByteBuffer UTF16(CharSequence text) {
        return UTF16(text, true);
    }

    /**
     * Encodes the specified text on the stack using UTF16 encoding and returns a {@code ByteBuffer} that points to the encoded text.
     *
     * <p>The buffer will have {@code alignment} equal to {@link Pointer#POINTER_SIZE POINTER_SIZE}.</p>
     *
     * @param text           the text to encode
     * @param nullTerminated if true, a null-terminator is included at the end of the encoded text
     */
    public ByteBuffer UTF16(CharSequence text, boolean nullTerminated) {
        int  length = MemoryUtil.memLengthUTF16(text, nullTerminated);
        long target = nmalloc(POINTER_SIZE, length);
        MemoryUtil.encodeUTF16Unsafe(text, nullTerminated, target);
        return MemoryUtil.wrapBufferByte(target, length).order(MemoryUtil.NATIVE_ORDER);
    }

    /**
     * Encodes the specified text on the stack using UTF16 encoding and returns the encoded text length, in bytes.
     *
     * <p>Use {@link #getPointerAddress} immediately after this method to get the encoded text address, which will have {@code alignment} equal to
     * {@link Pointer#POINTER_SIZE POINTER_SIZE}.</p>
     *
     * @param text           the text to encode
     * @param nullTerminated if true, a null-terminator is included at the end of the encoded text
     */
    public int nUTF16(CharSequence text, boolean nullTerminated) {
        long target = nmalloc(POINTER_SIZE, MemoryUtil.memLengthUTF16(text, nullTerminated));
        return MemoryUtil.encodeUTF16Unsafe(text, nullTerminated, target);
    }

    /** Like {@link #UTF16(CharSequence) UTF16}, but returns {@code null} if {@code text} is {@code null}. */
    @Nullable
    public ByteBuffer UTF16Safe(@Nullable CharSequence text) {
        return UTF16Safe(text, true);
    }

    /** Like {@link #UTF16(CharSequence, boolean) UTF16}, but returns {@code null} if {@code text} is {@code null}. */
    @Nullable
    public ByteBuffer UTF16Safe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? null : UTF16(text, nullTerminated);
    }

    /** Like {@link #nUTF16(CharSequence, boolean) nUTF16}, but returns 0 if {@code text} is {@code null}. */
    public int nUTF16Safe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? 0 : nUTF16(text, nullTerminated);
    }

    // -----------------------------------------------------
    // -----------------------------------------------------
    // -----------------------------------------------------

    /** Returns the stack of the current thread. */
    public static MemoryStack stackGet() {
        return TLS.get();
    }

    /**
     * Calls {@link #push} on the stack of the current thread.
     *
     * @return the stack of the current thread.
     */
    public static MemoryStack stackPush() {
        return stackGet().push();
    }

    /**
     * Calls {@link #pop} on the stack of the current thread.
     *
     * @return the stack of the current thread.
     */
    public static MemoryStack stackPop() {
        return stackGet().pop();
    }

    /** Thread-local version of {@link #nmalloc(int)}. */
    public static long nstackMalloc(int size) { return stackGet().nmalloc(size); }
    /** Thread-local version of {@link #nmalloc(int, int)}. */
    public static long nstackMalloc(int alignment, int size) { return stackGet().nmalloc(alignment, size); }
    /** Thread-local version of {@link #ncalloc}. */
    public static long nstackCalloc(int alignment, int num, int size) { return stackGet().ncalloc(alignment, num, size); }

    // -------------------------------------------------

    /** Thread-local version of {@link #malloc(int) malloc}. */
    public static ByteBuffer stackMalloc(int size) { return stackGet().malloc(size); }
    /** Thread-local version of {@link #calloc(int) calloc}. */
    public static ByteBuffer stackCalloc(int size) { return stackGet().calloc(size); }

    /** Thread-local version of {@link #bytes(byte)}. */
    public static ByteBuffer stackBytes(byte x) { return stackGet().bytes(x); }
    /** Thread-local version of {@link #bytes(byte, byte)}. */
    public static ByteBuffer stackBytes(byte x, byte y) { return stackGet().bytes(x, y); }
    /** Thread-local version of {@link #bytes(byte, byte, byte)}. */
    public static ByteBuffer stackBytes(byte x, byte y, byte z) { return stackGet().bytes(x, y, z); }
    /** Thread-local version of {@link #bytes(byte, byte, byte, byte)}. */
    public static ByteBuffer stackBytes(byte x, byte y, byte z, byte w) { return stackGet().bytes(x, y, z, w); }
    /** Thread-local version of {@link #bytes(byte...)}. */
    public static ByteBuffer stackBytes(byte... values) { return stackGet().bytes(values); }

    // -------------------------------------------------

    /** Thread-local version of {@link #mallocShort}. */
    public static ShortBuffer stackMallocShort(int size) { return stackGet().mallocShort(size); }
    /** Thread-local version of {@link #callocShort}. */
    public static ShortBuffer stackCallocShort(int size) { return stackGet().callocShort(size); }

    /** Thread-local version of {@link #shorts(short)}. */
    public static ShortBuffer stackShorts(short x) { return stackGet().shorts(x); }
    /** Thread-local version of {@link #shorts(short, short)}. */
    public static ShortBuffer stackShorts(short x, short y) { return stackGet().shorts(x, y); }
    /** Thread-local version of {@link #shorts(short, short, short)}. */
    public static ShortBuffer stackShorts(short x, short y, short z) { return stackGet().shorts(x, y, z); }
    /** Thread-local version of {@link #shorts(short, short, short, short)}. */
    public static ShortBuffer stackShorts(short x, short y, short z, short w) { return stackGet().shorts(x, y, z, w); }
    /** Thread-local version of {@link #shorts(short...)}. */
    public static ShortBuffer stackShorts(short... values) { return stackGet().shorts(values); }

    // -------------------------------------------------

    /** Thread-local version of {@link #mallocInt}. */
    public static IntBuffer stackMallocInt(int size) { return stackGet().mallocInt(size); }
    /** Thread-local version of {@link #callocInt}. */
    public static IntBuffer stackCallocInt(int size) { return stackGet().callocInt(size); }

    /** Thread-local version of {@link #ints(int)}. */
    public static IntBuffer stackInts(int x) { return stackGet().ints(x); }
    /** Thread-local version of {@link #ints(int, int)}. */
    public static IntBuffer stackInts(int x, int y) { return stackGet().ints(x, y); }
    /** Thread-local version of {@link #ints(int, int, int)}. */
    public static IntBuffer stackInts(int x, int y, int z) { return stackGet().ints(x, y, z); }
    /** Thread-local version of {@link #ints(int, int, int, int)}. */
    public static IntBuffer stackInts(int x, int y, int z, int w) { return stackGet().ints(x, y, z, w); }
    /** Thread-local version of {@link #ints(int...)}. */
    public static IntBuffer stackInts(int... values) { return stackGet().ints(values); }

    // -------------------------------------------------

    /** Thread-local version of {@link #mallocLong}. */
    public static LongBuffer stackMallocLong(int size) { return stackGet().mallocLong(size); }
    /** Thread-local version of {@link #callocLong}. */
    public static LongBuffer stackCallocLong(int size) { return stackGet().callocLong(size); }

    /** Thread-local version of {@link #longs(long)}. */
    public static LongBuffer stackLongs(long x) { return stackGet().longs(x); }
    /** Thread-local version of {@link #longs(long, long)}. */
    public static LongBuffer stackLongs(long x, long y) { return stackGet().longs(x, y); }
    /** Thread-local version of {@link #longs(long, long, long)}. */
    public static LongBuffer stackLongs(long x, long y, long z) { return stackGet().longs(x, y, z); }
    /** Thread-local version of {@link #longs(long, long, long, long)}. */
    public static LongBuffer stackLongs(long x, long y, long z, long w) { return stackGet().longs(x, y, z, w); }
    /** Thread-local version of {@link #longs(long...)}. */
    public static LongBuffer stackLongs(long... values) { return stackGet().longs(values); }

    // -------------------------------------------------

    /** Thread-local version of {@link #mallocFloat}. */
    public static FloatBuffer stackMallocFloat(int size) { return stackGet().mallocFloat(size); }
    /** Thread-local version of {@link #callocFloat}. */
    public static FloatBuffer stackCallocFloat(int size) { return stackGet().callocFloat(size); }

    /** Thread-local version of {@link #floats(float)}. */
    public static FloatBuffer stackFloats(float x) { return stackGet().floats(x); }
    /** Thread-local version of {@link #floats(float, float)}. */
    public static FloatBuffer stackFloats(float x, float y) { return stackGet().floats(x, y); }
    /** Thread-local version of {@link #floats(float, float, float)}. */
    public static FloatBuffer stackFloats(float x, float y, float z) { return stackGet().floats(x, y, z); }
    /** Thread-local version of {@link #floats(float, float, float, float)}. */
    public static FloatBuffer stackFloats(float x, float y, float z, float w) { return stackGet().floats(x, y, z, w); }
    /** Thread-local version of {@link #floats(float...)}. */
    public static FloatBuffer stackFloats(float... values) { return stackGet().floats(values); }

    // -------------------------------------------------

    /** Thread-local version of {@link #mallocDouble}. */
    public static DoubleBuffer stackMallocDouble(int size) { return stackGet().mallocDouble(size); }
    /** Thread-local version of {@link #callocDouble}. */
    public static DoubleBuffer stackCallocDouble(int size) { return stackGet().callocDouble(size); }

    /** Thread-local version of {@link #doubles(double)}. */
    public static DoubleBuffer stackDoubles(double x) { return stackGet().doubles(x); }
    /** Thread-local version of {@link #doubles(double, double)}. */
    public static DoubleBuffer stackDoubles(double x, double y) { return stackGet().doubles(x, y); }
    /** Thread-local version of {@link #doubles(double, double, double)}. */
    public static DoubleBuffer stackDoubles(double x, double y, double z) { return stackGet().doubles(x, y, z); }
    /** Thread-local version of {@link #doubles(double, double, double, double)}. */
    public static DoubleBuffer stackDoubles(double x, double y, double z, double w) { return stackGet().doubles(x, y, z, w); }
    /** Thread-local version of {@link #doubles(double...)}. */
    public static DoubleBuffer stackDoubles(double... values) { return stackGet().doubles(values); }

    // -------------------------------------------------

    /** Thread-local version of {@link #ASCII(CharSequence)}. */
    public static ByteBuffer stackASCII(CharSequence text) { return stackGet().ASCII(text); }

    /** Thread-local version of {@link #ASCII(CharSequence, boolean)}. */
    public static ByteBuffer stackASCII(CharSequence text, boolean nullTerminated) { return stackGet().ASCII(text, nullTerminated); }

    /** Thread-local version of {@link #UTF8(CharSequence)}. */
    public static ByteBuffer stackUTF8(CharSequence text) { return stackGet().UTF8(text); }

    /** Thread-local version of {@link #UTF8(CharSequence, boolean)}. */
    public static ByteBuffer stackUTF8(CharSequence text, boolean nullTerminated) { return stackGet().UTF8(text, nullTerminated); }

    /** Thread-local version of {@link #UTF16(CharSequence)}. */
    public static ByteBuffer stackUTF16(CharSequence text) { return stackGet().UTF16(text); }

    /** Thread-local version of {@link #UTF16(CharSequence, boolean)}. */
    public static ByteBuffer stackUTF16(CharSequence text, boolean nullTerminated) { return stackGet().UTF16(text, nullTerminated); }

    /** Thread-local version of {@link #ASCII(CharSequence)}. */
    @Nullable public static ByteBuffer stackASCIISafe(@Nullable CharSequence text) { return stackGet().ASCIISafe(text); }

    /** Thread-local version of {@link #ASCII(CharSequence, boolean)}. */
    @Nullable public static ByteBuffer stackASCIISafe(@Nullable CharSequence text, boolean nullTerminated) { return stackGet().ASCIISafe(text, nullTerminated); }

    /** Thread-local version of {@link #UTF8(CharSequence)}. */
    @Nullable public static ByteBuffer stackUTF8Safe(@Nullable CharSequence text) { return stackGet().UTF8Safe(text); }

    /** Thread-local version of {@link #UTF8(CharSequence, boolean)}. */
    @Nullable public static ByteBuffer stackUTF8Safe(@Nullable CharSequence text, boolean nullTerminated) { return stackGet().UTF8Safe(text, nullTerminated); }

    /** Thread-local version of {@link #UTF16(CharSequence)}. */
    @Nullable public static ByteBuffer stackUTF16Safe(@Nullable CharSequence text) { return stackGet().UTF16Safe(text); }

    /** Thread-local version of {@link #UTF16(CharSequence, boolean)}. */
    @Nullable public static ByteBuffer stackUTF16Safe(@Nullable CharSequence text, boolean nullTerminated) { return stackGet().UTF16Safe(text, nullTerminated); }

}