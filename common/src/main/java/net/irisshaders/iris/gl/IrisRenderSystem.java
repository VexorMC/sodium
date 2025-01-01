package net.irisshaders.iris.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.sampler.SamplerLimits;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 * This class is responsible for abstracting calls to OpenGL and asserting that calls are run on the render thread.
 */
public class IrisRenderSystem {
	private static final int[] emptyArray = new int[SamplerLimits.get().getMaxTextureUnits()];
	private static Matrix4f backupProjection;
	private static DSAAccess dsaState;
	private static boolean hasMultibind;
	private static boolean supportsCompute;
	private static boolean supportsTesselation;
	private static int polygonMode = GL11.GL_FILL;
	private static int backupPolygonMode = GL11.GL_FILL;
	private static int[] samplers;

	public static void initRenderer() {
		if (GLContext.getCapabilities().OpenGL45) {
			dsaState = new DSACore();
			Iris.logger.info("OpenGL 4.5 detected, enabling DSA.");
		} else if (GLContext.getCapabilities().GL_ARB_direct_state_access) {
			dsaState = new DSAARB();
			Iris.logger.info("ARB_direct_state_access detected, enabling DSA.");
		} else {
			dsaState = new DSAUnsupported();
			Iris.logger.info("DSA support not detected.");
		}

		hasMultibind = GLContext.getCapabilities().OpenGL45 || GLContext.getCapabilities().GL_ARB_multi_bind;

		supportsCompute = true;
		supportsTesselation = GLContext.getCapabilities().GL_ARB_tessellation_shader || GLContext.getCapabilities().OpenGL40;

		samplers = new int[SamplerLimits.get().getMaxTextureUnits()];
	}

	public static void getIntegerv(int pname, int[] params) {
        IntBuffer buf = BufferUtils.createIntBuffer(params.length);
        GL11.glGetInteger(pname, buf);
        buf.get(params);
    }

	public static void getFloatv(int pname, float[] params) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(params.length);
        GL11.glGetFloat(pname, buf);
        buf.get(params);
    }

	public static void generateMipmaps(int texture, int mipmapTarget) {
		dsaState.generateMipmaps(texture, mipmapTarget);
	}

	public static void bindAttributeLocation(int program, int index, CharSequence name) {
		GL20.glBindAttribLocation(program, index, name);
	}

	public static void texImage1D(int texture, int target, int level, int internalformat, int width, int border, int format, int type, @Nullable ByteBuffer pixels) {
		IrisRenderSystem.bindTextureForSetup(target, texture);
		GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
	}

	public static void texImage2D(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, @Nullable ByteBuffer pixels) {
		IrisRenderSystem.bindTextureForSetup(target, texture);
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	public static void texImage3D(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, @Nullable ByteBuffer pixels) {
		IrisRenderSystem.bindTextureForSetup(target, texture);
        GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
	}

	public static void uniformMatrix4fv(int location, boolean transpose, FloatBuffer matrix) {
		GL20.glUniformMatrix4(location, transpose, matrix);
	}

	public static void copyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
		GL11.glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
	}

	public static void uniform1f(int location, float v0) {
		GL20.glUniform1f(location, v0);
	}

	public static void uniform2f(int location, float v0, float v1) {
		GL20.glUniform2f(location, v0, v1);
	}

	public static void uniform2i(int location, int v0, int v1) {
		GL20.glUniform2i(location, v0, v1);
	}

	public static void uniform3f(int location, float v0, float v1, float v2) {
		GL20.glUniform3f(location, v0, v1, v2);
	}

	public static void uniform3i(int location, int v0, int v1, int v2) {
		GL20.glUniform3i(location, v0, v1, v2);
	}

	public static void uniform4f(int location, float v0, float v1, float v2, float v3) {
		GL20.glUniform4f(location, v0, v1, v2, v3);
	}

	public static void uniform4i(int location, int v0, int v1, int v2, int v3) {
		GL20.glUniform4i(location, v0, v1, v2, v3);
	}

	public static void texParameteriv(int texture, int target, int pname, int[] params) {
		dsaState.texParameteriv(texture, target, pname, params);
	}

	public static void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
		dsaState.copyTexSubImage2D(destTexture, target, i, i1, i2, i3, i4, width, height);
	}

	public static void texParameteri(int texture, int target, int pname, int param) {
		dsaState.texParameteri(texture, target, pname, param);
	}

	public static void texParameterf(int texture, int target, int pname, float param) {
		dsaState.texParameterf(texture, target, pname, param);
	}

	public static String getProgramInfoLog(int program) {
		return GL20.glGetProgramInfoLog(program, 1000);
	}

	public static String getShaderInfoLog(int shader) {
		return GL20.glGetShaderInfoLog(shader, 1000);
	}

	public static void drawBuffers(int framebuffer, int[] buffers) {
		dsaState.drawBuffers(framebuffer, buffers);
	}

	public static void readBuffer(int framebuffer, int buffer) {
		dsaState.readBuffer(framebuffer, buffer);
	}

	public static String getActiveUniform(int program, int index, int size, IntBuffer sizeBuf, IntBuffer typeBuf) {
	}

	public static void readPixels(int x, int y, int width, int height, int format, int type, float[] pixels) {
		GL20.glReadPixels(x, y, width, height, format, type, pixels);
	}

	public static void bufferData(int target, float[] data, int usage) {
		GL20.glBufferData(target, data, usage);
	}

	public static int bufferStorage(int target, float[] data, int usage) {
		return dsaState.bufferStorage(target, data, usage);
	}

	public static void bufferStorage(int target, long size, int flags) {
		// The ARB version is identical to GL44 and redirects, so this should work on ARB as well.
		GL45.glBufferStorage(target, size, flags);
	}

	public static void bindBufferBase(int target, Integer index, int buffer) {
		GL43.glBindBufferBase(target, index, buffer);
	}

	public static void vertexAttrib4f(int index, float v0, float v1, float v2, float v3) {
		GL20.glVertexAttrib4f(index, v0, v1, v2, v3);
	}

	public static void detachShader(int program, int shader) {
		GL20.glDetachShader(program, shader);
	}

	public static void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels) {
		dsaState.framebufferTexture2D(fb, fbtarget, attachment, target, texture, levels);
	}

	public static int getTexParameteri(int texture, int target, int pname) {
		return dsaState.getTexParameteri(texture, target, pname);
	}

	public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
		if (GLContext.getCapabilities().OpenGL42 || GLContext.getCapabilities().GL_ARB_shader_image_load_store) {
			GL42.glBindImageTexture(unit, texture, level, layered, layer, access, format);
		} else {
			EXTShaderImageLoadStore.glBindImageTextureEXT(unit, texture, level, layered, layer, access, format);
		}
	}

	public static int getMaxImageUnits() {
		if (GLContext.getCapabilities().OpenGL42 || GLContext.getCapabilities().GL_ARB_shader_image_load_store) {
			return GlStateManager._getInteger(GL42.GL_MAX_IMAGE_UNITS);
		} else if (GLContext.getCapabilities().GL_EXT_shader_image_load_store) {
			return GlStateManager._getInteger(EXTShaderImageLoadStore.GL_MAX_IMAGE_UNITS_EXT);
		} else {
			return 0;
		}
	}

	public static boolean supportsSSBO() {
		return GLContext.getCapabilities().OpenGL44 || (GLContext.getCapabilities().GL_ARB_shader_storage_buffer_object && GLContext.getCapabilities().GL_ARB_buffer_storage);
	}

	public static boolean supportsImageLoadStore() {
		return GLContext.getCapabilities().glBindImageTexture != 0L || GLContext.getCapabilities().OpenGL42 || ((GLContext.getCapabilities().GL_ARB_shader_image_load_store || GLContext.getCapabilities().GL_EXT_shader_image_load_store) && GLContext.getCapabilities().GL_ARB_buffer_storage);
	}

	public static void genBuffers(int[] buffers) {
		GL43.glGenBuffers(buffers);
	}

	public static void clearBufferSubData(int glShaderStorageBuffer, int glR8, long offset, long size, int glRed, int glByte, int[] ints) {
		GL43.glClearBufferSubData(glShaderStorageBuffer, glR8, offset, size, glRed, glByte, ints);
	}

	public static void getProgramiv(int program, int value, int[] storage) {
		GL20.glGetProgramiv(program, value, storage);
	}

	public static void dispatchCompute(int workX, int workY, int workZ) {
		GL45.glDispatchCompute(workX, workY, workZ);
	}

	public static void dispatchCompute(Vector3i workGroups) {
		GL45.glDispatchCompute(workGroups.x, workGroups.y, workGroups.z);
	}

	public static void memoryBarrier(int barriers) {
		RenderSystem.assertOnRenderThreadOrInit();

		if (supportsCompute) {
			GL45.glMemoryBarrier(barriers);
		}
	}

	public static boolean supportsBufferBlending() {
		return GLContext.getCapabilities().GL_ARB_draw_buffers_blend || GLContext.getCapabilities().OpenGL40;
	}

	public static void disableBufferBlend(int buffer) {
		GL20.glDisablei(GL20.GL_BLEND, buffer);
		((BooleanStateExtended) GlStateManagerAccessor.getBLEND().mode).setUnknownState();
	}

	public static void enableBufferBlend(int buffer) {
		GL20.glEnablei(GL20.GL_BLEND, buffer);
		((BooleanStateExtended) GlStateManagerAccessor.getBLEND().mode).setUnknownState();
	}

	public static void blendFuncSeparatei(int buffer, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		ARBDrawBuffersBlend.glBlendFuncSeparateiARB(buffer, srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	// These functions are deprecated and unavailable in the core profile.

	public static void bindTextureToUnit(int target, int unit, int texture) {
		dsaState.bindTextureToUnit(target, unit, texture);
	}

	public static int getUniformBlockIndex(int program, String uniformBlockName) {
		return GL20.glGetUniformBlockIndex(program, uniformBlockName);
	}

	public static void uniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
		GL20.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	public static void setShadowProjection(Matrix4f shadowProjection) {
		backupProjection = RenderSystem.getProjectionMatrix();
		RenderSystem.setProjectionMatrix(shadowProjection, VertexSorting.ORTHOGRAPHIC_Z);
	}

	public static void restorePlayerProjection() {
		RenderSystem.setProjectionMatrix(backupProjection, VertexSorting.DISTANCE_TO_ORIGIN);
		backupProjection = null;
	}

	public static void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
		dsaState.blitFramebuffer(source, dest, offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
	}

	public static int createFramebuffer() {
		return dsaState.createFramebuffer();
	}

	public static int createTexture(int target) {
		return dsaState.createTexture(target);
	}

	public static void bindTextureForSetup(int glType, int glId) {
		GL30.glBindTexture(glType, glId);
	}

	public static boolean supportsCompute() {
		return supportsCompute;
	}

	public static boolean supportsTesselation() {
		return supportsTesselation;
	}

	public static int genSampler() {
		return GL33.glGenSamplers();
	}

	public static void destroySampler(int glId) {
		GL33.glDeleteSamplers(glId);
	}

	public static void bindSamplerToUnit(int unit, int sampler) {
		if (samplers[unit] == sampler) {
			return;
		}

		GL33.glBindSampler(unit, sampler);

		samplers[unit] = sampler;
	}

	public static void unbindAllSamplers() {
		boolean usedASampler = false;
		for (int i = 0; i < samplers.length; i++) {
			if (samplers[i] != 0) {
				usedASampler = true;
				if (!hasMultibind) GL33.glBindSampler(i, 0);
				samplers[i] = 0;
			}
		}
		if (usedASampler && hasMultibind) {
			GL45.glBindSamplers(0, emptyArray);
		}
	}


	public static void samplerParameteri(int sampler, int pname, int param) {
		GL33.glSamplerParameteri(sampler, pname, param);
	}

	public static void samplerParameterf(int sampler, int pname, float param) {
		GL33.glSamplerParameterf(sampler, pname, param);
	}

	public static void samplerParameteriv(int sampler, int pname, int[] params) {
		GL33.glSamplerParameteriv(sampler, pname, params);
	}

	public static long getVRAM() {
		if (GLContext.getCapabilities().GL_NVX_gpu_memory_info) {
			return GL20.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX) * 1024L;
		} else {
			return 4294967296L;
		}
	}

	public static void deleteBuffers(int glId) {
		GL43.glDeleteBuffers(glId);
	}

	public static void setPolygonMode(int mode) {
		if (mode != polygonMode) {
			polygonMode = mode;
			GL43.glPolygonMode(GL43.GL_FRONT_AND_BACK, mode);
		}
	}

	public static void overridePolygonMode() {
		backupPolygonMode = polygonMode;
		setPolygonMode(GL43.GL_FILL);
	}

	public static void restorePolygonMode() {
		setPolygonMode(backupPolygonMode);
		backupPolygonMode = GL43.GL_FILL;
	}

	public static void dispatchComputeIndirect(long offset) {
		GL43.glDispatchComputeIndirect(offset);
	}

	public static void bindBuffer(int target, int buffer) {
		GL46.glBindBuffer(target, buffer);
	}

	public static int createBuffers() {
		return dsaState.createBuffers();
	}

	private static boolean cullingState;

	public static void backupAndDisableCullingState(boolean b) {
		cullingState = MinecraftClient.getInstance().smartCull;
		MinecraftClient.getInstance().smartCull = MinecraftClient.getInstance().smartCull && !b;
	}

	public static void restoreCullingState() {
		MinecraftClient.getInstance().smartCull = cullingState;
		cullingState = true;
	}

	public interface DSAAccess {
		void generateMipmaps(int texture, int target);

		void texParameteri(int texture, int target, int pname, int param);

		void texParameterf(int texture, int target, int pname, float param);

		void texParameteriv(int texture, int target, int pname, int[] params);

		void readBuffer(int framebuffer, int buffer);

		void drawBuffers(int framebuffer, int[] buffers);

		int getTexParameteri(int texture, int target, int pname);

		void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height);

		void bindTextureToUnit(int target, int unit, int texture);

		int bufferStorage(int target, float[] data, int usage);

		void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter);

		void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels);

		int createFramebuffer();

		int createTexture(int target);

		int createBuffers();
	}

	public static class DSACore extends DSAARB {

	}

	public static class DSAARB extends DSAUnsupported {

		@Override
		public void generateMipmaps(int texture, int target) {
			ARBDirectStateAccess.glGenerateTextureMipmap(texture);
		}

		@Override
		public void texParameteri(int texture, int target, int pname, int param) {
			ARBDirectStateAccess.glTextureParameteri(texture, pname, param);
		}

		@Override
		public void texParameterf(int texture, int target, int pname, float param) {
			ARBDirectStateAccess.glTextureParameterf(texture, pname, param);
		}

		@Override
		public void texParameteriv(int texture, int target, int pname, int[] params) {
			ARBDirectStateAccess.glTextureParameteriv(texture, pname, params);
		}

		@Override
		public void readBuffer(int framebuffer, int buffer) {
			ARBDirectStateAccess.glNamedFramebufferReadBuffer(framebuffer, buffer);
		}

		@Override
		public void drawBuffers(int framebuffer, int[] buffers) {
			ARBDirectStateAccess.glNamedFramebufferDrawBuffers(framebuffer, buffers);
		}

		@Override
		public int getTexParameteri(int texture, int target, int pname) {
			return ARBDirectStateAccess.glGetTextureParameteri(texture, pname);
		}

		@Override
		public void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
			ARBDirectStateAccess.glCopyTextureSubImage2D(destTexture, i, i1, i2, i3, i4, width, height);
		}

		@Override
		public void bindTextureToUnit(int target, int unit, int texture) {
			if (GlStateManagerAccessor.getTEXTURES()[unit].binding == texture) {
				return;
			}

			ARBDirectStateAccess.glBindTextureUnit(unit, texture);

			// Manually fix GLStateManager bindings...
			GlStateManagerAccessor.getTEXTURES()[unit].binding = texture;
		}

		@Override
		public int bufferStorage(int target, float[] data, int usage) {
			int buffer = GL45.glCreateBuffers();
			GL45.glNamedBufferData(buffer, data, usage);
			return buffer;
		}

		@Override
		public int createBuffers() {
			return ARBDirectStateAccess.glCreateBuffers();
		}

		@Override
		public void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
			ARBDirectStateAccess.glBlitNamedFramebuffer(source, dest, offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
		}

		@Override
		public void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels) {
			ARBDirectStateAccess.glNamedFramebufferTexture(fb, attachment, texture, levels);
		}

		@Override
		public int createFramebuffer() {
			return ARBDirectStateAccess.glCreateFramebuffers();
		}

		@Override
		public int createTexture(int target) {
			return ARBDirectStateAccess.glCreateTextures(target);
		}
	}

	public static class DSAUnsupported implements DSAAccess {
		@Override
		public void generateMipmaps(int texture, int target) {
			GlStateManager._bindTexture(texture);
			GL20.glGenerateMipmap(target);
		}

		@Override
		public void texParameteri(int texture, int target, int pname, int param) {
			bindTextureForSetup(target, texture);
			GL20.glTexParameteri(target, pname, param);
		}

		@Override
		public void texParameterf(int texture, int target, int pname, float param) {
			bindTextureForSetup(target, texture);
			GL20.glTexParameterf(target, pname, param);
		}

		@Override
		public void texParameteriv(int texture, int target, int pname, int[] params) {
			bindTextureForSetup(target, texture);
			GL20.glTexParameteriv(target, pname, params);
		}

		@Override
		public void readBuffer(int framebuffer, int buffer) {
			GlStateManager._glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebuffer);
			GL20.glReadBuffer(buffer);
		}

		@Override
		public void drawBuffers(int framebuffer, int[] buffers) {
			GlStateManager._glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebuffer);
			GL20.glDrawBuffers(buffers);
		}

		@Override
		public int getTexParameteri(int texture, int target, int pname) {
			bindTextureForSetup(target, texture);
			return GL20.glGetTexParameteri(target, pname);
		}

		@Override
		public void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
			int previous = GlStateManagerAccessor.getTEXTURES()[GlStateManagerAccessor.getActiveTexture()].binding;
			GlStateManager._bindTexture(destTexture);
			GL20.glCopyTexSubImage2D(target, i, i1, i2, i3, i4, width, height);
			GlStateManager._bindTexture(previous);
		}

		@Override
		public void bindTextureToUnit(int target, int unit, int texture) {
			int activeTexture = GlStateManager._getActiveTexture();
			GlStateManager._activeTexture(GL30.GL_TEXTURE0 + unit);
			bindTextureForSetup(target, texture);
			GlStateManager._activeTexture(activeTexture);
		}

		@Override
		public int bufferStorage(int target, float[] data, int usage) {
			int buffer = GlStateManager._glGenBuffers();
			GlStateManager._glBindBuffer(target, buffer);
			bufferData(target, data, usage);
			GlStateManager._glBindBuffer(target, 0);

			return buffer;
		}

		@Override
		public void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
			GlStateManager._glBindFramebuffer(GL20.GL_READ_FRAMEBUFFER, source);
			GlStateManager._glBindFramebuffer(GL20.GL_DRAW_FRAMEBUFFER, dest);
			GL20.glBlitFramebuffer(offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
		}

		@Override
		public void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels) {
			GlStateManager._glBindFramebuffer(fbtarget, fb);
			GL20.glFramebufferTexture2D(fbtarget, attachment, target, texture, levels);
		}

		@Override
		public int createFramebuffer() {
			int framebuffer = GlStateManager.glGenFramebuffers();
			GlStateManager._glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebuffer);
			return framebuffer;
		}

		@Override
		public int createTexture(int target) {
			int texture = GlStateManager._genTexture();
			GlStateManager._bindTexture(texture);
			return texture;
		}

		@Override
		public int createBuffers() {
			return GlStateManager._glGenBuffers();
		}
	}

		/*
	public static void bindTextures(int startingTexture, int[] bindings) {
		if (hasMultibind) {
			ARBMultiBind.glBindTextures(startingTexture, bindings);
		} else if (dsaState != DSAState.NONE) {
			for (int binding : bindings) {
				ARBDirectStateAccess.glBindTextureUnit(startingTexture, binding);
				startingTexture++;
			}
		} else {
			for (int binding : bindings) {
				GlStateManager._activeTexture(startingTexture);
				GlStateManager._bindTexture(binding);
				startingTexture++;
			}
		}
	}
	 */
}
