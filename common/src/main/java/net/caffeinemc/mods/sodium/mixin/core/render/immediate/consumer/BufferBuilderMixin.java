package net.caffeinemc.mods.sodium.mixin.core.render.immediate.consumer;

import dev.lunasa.compat.lwjgl3.MemoryStack;
import dev.lunasa.compat.lwjgl3.MemoryUtil;
import net.caffeinemc.mods.sodium.client.render.vertex.buffer.BufferBuilderExtension;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements VertexBufferWriter, BufferBuilderExtension {
    @Shadow
    private int vertexCount;

    @Shadow
    @Mutable
    private ByteBuffer buffer;

    @Unique
    private VertexFormat format;

    @Inject(
            method = "begin",
            at = @At(value = "TAIL")
    )
    private void onFormatChanged(int drawMode, VertexFormat format, CallbackInfo ci) {
        this.format = format;
    }

    @Override
    public boolean canUseIntrinsics() {
        return this.format != null;
    }

    @Override
    public void push(MemoryStack stack, long src, int count, VertexFormat format) {
        int length = count * this.vertexCount;

        // Ensure buffer has enough capacity for the new data
        reserveBuffer(length);

        if (format.equals(this.format)) {
            // Layout is the same, perform direct memory copy
            // Using JNI for memory copying between direct buffers
            copyMemoryDirect(src, buffer, length);
        }
    }

    /**
     * Ensures the buffer has enough capacity to accommodate the new data.
     */
    @Unique
    private void reserveBuffer(int requiredCapacity) {
        if (buffer.remaining() < requiredCapacity) {
            // Allocate a new larger buffer if necessary
            int newCapacity = Math.max(buffer.capacity() * 2, buffer.capacity() + requiredCapacity);
            ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity).order(ByteOrder.nativeOrder());
            buffer.flip(); // Prepare old buffer for reading
            newBuffer.put(buffer); // Copy old data into the new buffer
            buffer = newBuffer;
        }
    }

    /**
     * Copies memory from the source address into the buffer using native memory operations.
     */
    @Unique
    private void copyMemoryDirect(long src, ByteBuffer dst, int length) {
        // Assuming src is a pointer, direct buffer required to manipulate memory
        for (int i = 0; i < length; i++) {
            dst.put((byte) MemoryUtil.memGetByte(src + i)); // Mimic memory copy
        }
    }
}