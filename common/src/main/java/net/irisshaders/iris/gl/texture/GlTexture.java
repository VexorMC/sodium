package net.irisshaders.iris.gl.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.GlResource;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.shaderpack.texture.TextureFilteringData;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.function.IntSupplier;

public class GlTexture extends GlResource implements TextureAccess {
	private final TextureType target;

	public GlTexture(TextureType target, int sizeX, int sizeY, int sizeZ, int internalFormat, int format, int pixelType, byte[] pixels, TextureFilteringData filteringData) {
		super(GL11.glGenTextures());
		IrisRenderSystem.bindTextureForSetup(target.getGlType(), getGlId());

		TextureUploadHelper.resetTextureUploadState();

		ByteBuffer buffer = MemoryUtil.memAlloc(pixels.length);
		buffer.put(pixels);
		buffer.flip();
		target.apply(this.getGlId(), sizeX, sizeY, sizeZ, internalFormat, format, pixelType, buffer);
		MemoryUtil.memFree(buffer);

		int texture = this.getGlId();

		IrisRenderSystem.texParameteri(texture, target.getGlType(), GL11.GL_TEXTURE_MIN_FILTER, filteringData.shouldBlur() ? GL11.GL_LINEAR : GL11.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, target.getGlType(), GL11.GL_TEXTURE_MAG_FILTER, filteringData.shouldBlur() ? GL11.GL_LINEAR : GL11.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, target.getGlType(), GL11.GL_TEXTURE_WRAP_S, filteringData.shouldClamp() ? GL12.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);

		if (sizeY > 0) {
			IrisRenderSystem.texParameteri(texture, target.getGlType(), GL11.GL_TEXTURE_WRAP_T, filteringData.shouldClamp() ? GL12.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
		}

		if (sizeZ > 0) {
			IrisRenderSystem.texParameteri(texture, target.getGlType(), GL12.GL_TEXTURE_WRAP_R, filteringData.shouldClamp() ? GL12.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
		}

		IrisRenderSystem.texParameteri(texture, target.getGlType(), GL12.GL_TEXTURE_MAX_LEVEL, 0);
		IrisRenderSystem.texParameteri(texture, target.getGlType(), GL12.GL_TEXTURE_MIN_LOD, 0);
		IrisRenderSystem.texParameteri(texture, target.getGlType(), GL12.GL_TEXTURE_MAX_LOD, 0);
		IrisRenderSystem.texParameterf(texture, target.getGlType(), GL14.GL_TEXTURE_LOD_BIAS, 0.0F);

		IrisRenderSystem.bindTextureForSetup(target.getGlType(), 0);

		this.target = target;
	}

	public TextureType getTarget() {
		return target;
	}

	public void bind(int unit) {
		IrisRenderSystem.bindTextureToUnit(target.getGlType(), unit, getGlId());
	}

	@Override
	public TextureType getType() {
		return target;
	}

	@Override
	public IntSupplier getTextureId() {
		return this::getGlId;
	}

	@Override
	protected void destroyInternal() {
		GL11.glDeleteTextures(getGlId());
	}
}
