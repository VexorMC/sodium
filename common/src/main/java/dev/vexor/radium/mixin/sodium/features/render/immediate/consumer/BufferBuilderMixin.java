package dev.vexor.radium.mixin.sodium.features.render.immediate.consumer;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements VertexBufferWriter, VertexConsumer {
    @Shadow
    private int vertexCount;

    @Shadow
    private ByteBuffer buffer;

    @Shadow
    private VertexFormat format;

    @Unique
    public void sodium$duplicateVertex() {
        if (this.vertexCount == 0) {
            return;
        }

        int position = this.buffer.position();
        this.buffer.position(position - this.format.getVertexSize());
        byte[] vertexData = new byte[this.format.getVertexSize()];
        this.buffer.get(vertexData);
        this.buffer.position(position);
        this.buffer.put(vertexData);

        this.vertexCount++;
    }

    @Unique
    public void sodium$push(MemoryStack stack, long src, int count, VertexFormat format) {
        int length = count * this.format.getVertexSize();
        long dst = MemoryUtil.memAddress(buffer) + buffer.position();

        if (format == this.format) {
            MemoryUtil.memCopy(src, dst, length);
            buffer.position(buffer.position() + length);
        } else {
            this.copySlow(src, dst, count, format);
        }

        this.vertexCount += count;
    }

    @Unique
    private void copySlow(long src, long dst, int count, VertexFormat format) {
        int length = count * this.format.getVertexSize();
        MemoryUtil.memCopy(src, dst, length);
    }
}
