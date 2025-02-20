package net.caffeinemc.mods.sodium.client.render.vertex;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import org.jetbrains.annotations.Nullable;

public class VertexConsumerUtils {
    /**
     * Attempt to convert a {@link VertexConsumer} into a {@link VertexBufferWriter}. If this fails, return null
     * and log a message.
     * @param consumer the consumer to convert
     * @return a {@link VertexBufferWriter}, or null if the consumer does not support this
     */
    public static @Nullable VertexBufferWriter convertOrLog(VertexConsumer consumer) {
        VertexBufferWriter writer = VertexBufferWriter.tryOf(consumer);

        if (writer == null) {
            // bad consumer
        }

        return writer;
    }
}