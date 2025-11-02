package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexWriterFallback;
import me.jellysquid.mods.sodium.client.model.vertex.type.BlittableVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.type.VanillaVertexType;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.minecraft.client.render.BufferBuilder;

public class ExtendedQuadVertexType implements VanillaVertexType<QuadVertexSink>, BlittableVertexType<QuadVertexSink> {
	public static final ExtendedQuadVertexType INSTANCE = new ExtendedQuadVertexType();

	@Override
	public QuadVertexSink createFallbackWriter(BufferBuilder BufferBuilder) {
		return new QuadVertexWriterFallback(BufferBuilder);
	}

	@Override
	public QuadVertexSink createBufferWriter(VertexBufferView buffer, boolean direct) {
		return direct ? new EntityVertexBufferWriterUnsafe(buffer) : new EntityVertexBufferWriterNio(buffer);
	}

	@Override
	public VertexFormat getVertexFormat() {
		return IrisVertexFormats.ENTITY;
	}

	public BlittableVertexType<QuadVertexSink> asBlittable() {
		return this;
	}
}
