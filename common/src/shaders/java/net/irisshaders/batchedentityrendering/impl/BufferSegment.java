package net.irisshaders.batchedentityrendering.impl;

import net.minecraft.client.renderer.RenderType;

import java.nio.ByteBuffer;

public record BufferSegment(ByteBuffer meshData,
                            RenderType type) {
}
