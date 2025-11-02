package net.coderbot.iris.compat.sodium.impl.vertex_format;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import org.lwjgl.opengl.GL20C;

public class IrisGlVertexAttributeFormat {
	public static final GlVertexAttributeFormat BYTE =
			new GlVertexAttributeFormat(GL20C.GL_BYTE, 1);
	public static final GlVertexAttributeFormat SHORT = new GlVertexAttributeFormat(GL20C.GL_SHORT, 2);
}
