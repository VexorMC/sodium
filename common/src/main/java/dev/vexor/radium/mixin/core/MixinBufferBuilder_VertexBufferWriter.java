package dev.vexor.radium.mixin.core;

import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder_VertexBufferWriter implements VertexBufferWriter {
    @Shadow private ByteBuffer buffer;
    @Shadow private VertexFormat format;
    @Shadow private int vertexCount;

    @Shadow protected abstract void grow(int size);

    @Override
    public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
        int vertexSize = this.format.getVertexSize(); // bytes per vertex
        int totalBytes = count * vertexSize;

        int oldPos = this.buffer.position();
        int newPos = oldPos + totalBytes;

        int capacity = this.buffer.capacity();
        if (newPos > capacity) {
            int growInts = ((newPos - capacity) + 3) / 4;
            this.grow(growInts);
        }

        if (newPos > this.buffer.limit()) {
            this.buffer.limit(newPos);
        }

        long dstAddr = MemoryUtil.memAddress(this.buffer) + oldPos;
        MemoryUtil.memCopy(ptr, dstAddr, totalBytes);

        this.buffer.position(newPos);
        this.vertexCount += count;
    }
}
