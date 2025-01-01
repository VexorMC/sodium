package net.irisshaders.iris.gl.texture;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.GlVersion;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import java.util.Locale;
import java.util.Optional;

public enum PixelFormat {
	RED(1, GL11.GL_RED, GlVersion.GL_11, false),
	RG(2, GL30.GL_RG, GlVersion.GL_30, false),
	RGB(3, GL11.GL_RGB, GlVersion.GL_11, false),
	BGR(3, GL12.GL_BGR, GlVersion.GL_12, false),
	RGBA(4, GL11.GL_RGBA, GlVersion.GL_11, false),
	BGRA(4, GL12.GL_BGRA, GlVersion.GL_12, false),
	RED_INTEGER(1, GL30.GL_RED_INTEGER, GlVersion.GL_30, true),
	RG_INTEGER(2, GL30.GL_RG_INTEGER, GlVersion.GL_30, true),
	RGB_INTEGER(3, GL30.GL_RGB_INTEGER, GlVersion.GL_30, true),
	BGR_INTEGER(3, GL30.GL_BGR_INTEGER, GlVersion.GL_30, true),
	RGBA_INTEGER(4, GL30.GL_RGBA_INTEGER, GlVersion.GL_30, true),
	BGRA_INTEGER(4, GL30.GL_BGRA_INTEGER, GlVersion.GL_30, true);

	private final int componentCount;
	private final int glFormat;
	private final GlVersion minimumGlVersion;
	private final boolean isInteger;

	PixelFormat(int componentCount, int glFormat, GlVersion minimumGlVersion, boolean isInteger) {
		this.componentCount = componentCount;
		this.glFormat = glFormat;
		this.minimumGlVersion = minimumGlVersion;
		this.isInteger = isInteger;
	}

	public static Optional<PixelFormat> fromString(String name) {
		try {
			return Optional.of(PixelFormat.valueOf(name.toUpperCase(Locale.US)));
		} catch (IllegalArgumentException e) {
			Iris.logger.error("Looking for an illegal pixel format: " + name.toUpperCase(Locale.US));
			return Optional.empty();
		}
	}

	public int getComponentCount() {
		return componentCount;
	}

	public int getGlFormat() {
		return glFormat;
	}

	public GlVersion getMinimumGlVersion() {
		return minimumGlVersion;
	}

	public boolean isInteger() {
		return isInteger;
	}
}
