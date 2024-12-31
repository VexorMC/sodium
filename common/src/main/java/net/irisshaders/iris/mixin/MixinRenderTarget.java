package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.targets.Blaze3dRenderTargetExt;
import org.lwjgl.opengl.GL43C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows Iris to detect when the depth texture was re-created, so we can re-attach it
 * to the shader framebuffers. See DeferredWorldRenderingPipeline and RenderTargets.
 */
@Mixin(RenderTarget.class)
public class MixinRenderTarget implements Blaze3dRenderTargetExt {
	@Shadow
	protected int depthBufferId;

	@Shadow
	protected int colorTextureId;
	@Shadow
	public int frameBufferId;
	@Unique
	private int iris$depthBufferVersion;
	@Unique
	private int iris$colorBufferVersion;

	@Inject(method = "destroyBuffers()V", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$depthBufferVersion++;
		iris$colorBufferVersion++;
	}

	@Inject(method = "createBuffers", at = @At(value = "RETURN"))
	private void nameDepthBuffer(int i, int j, boolean bl, CallbackInfo ci) {
		GLDebug.nameObject(GL43C.GL_TEXTURE, this.depthBufferId, "Main depth texture");
		GLDebug.nameObject(GL43C.GL_TEXTURE, this.colorTextureId, "Main color texture");
		GLDebug.nameObject(GL43C.GL_FRAMEBUFFER, this.frameBufferId, "Main framebuffer");
	}

	@Override
	public int iris$getDepthBufferVersion() {
		return iris$depthBufferVersion;
	}

	@Override
	public int iris$getColorBufferVersion() {
		return iris$colorBufferVersion;
	}
}
