package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

/**
 * Allows Iris to detect when the depth texture was re-created, so we can re-attach it
 * to the shader framebuffers. See DeferredWorldRenderingPipeline and RenderTargets.
 */
@Mixin(Framebuffer.class)
public class MixinRenderTarget implements Blaze3dRenderTargetExt {
    @Shadow
    public boolean useDepthAttachment;

    public int iris$depthTextureId = -1;

    private int iris$depthBufferVersion;
	private int iris$colorBufferVersion;
	@Inject(method = "delete()V", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$depthBufferVersion++;
		iris$colorBufferVersion++;
	}

	@Override
	public int iris$getDepthBufferVersion() {
		return iris$depthBufferVersion;
	}

	@Override
	public int iris$getColorBufferVersion() {
		return iris$colorBufferVersion;
	}

    @Override
    public boolean getIris$useDepth() {
        return this.useDepthAttachment;
    }

    @Override
    public int getIris$depthTextureId() {
        return this.iris$depthTextureId;
    }

    @Inject(method="delete()V", at=@At(value="FIELD", target="Lnet/minecraft/client/gl/Framebuffer;depthAttachment:I", shift = At.Shift.BEFORE, ordinal = 0), remap = false)
    private void iris$deleteDepthBuffer(CallbackInfo ci) {
        if(this.iris$depthTextureId > -1 ) {
            GlStateManager.deleteTexture(this.iris$depthTextureId);
            this.iris$depthTextureId = -1;
        }
    }

    @Inject(method="attachTexture(II)V", at=@At(value="FIELD", target="Lnet/minecraft/client/gl/Framebuffer;depthAttachment:I", shift=At.Shift.BEFORE, ordinal = 0))
    private void iris$createDepthTextureID(int width, int height, CallbackInfo ci) {
        if (this.useDepthAttachment) {
            this.iris$depthTextureId = GL11.glGenTextures();
        }
    }

    @Inject(method="attachTexture", at=@At(value="FIELD", target="Lnet/minecraft/client/gl/Framebuffer;useDepthAttachment:Z", shift=At.Shift.BEFORE, ordinal = 1))
    private void iris$createDepthTexture(int width, int height, CallbackInfo ci) {
        if(this.useDepthAttachment) {
            if(this.iris$depthTextureId == -1) {
                this.iris$depthTextureId = GL11.glGenTextures();
            }
            GlStateManager.bindTexture(this.iris$depthTextureId);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, 0);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (IntBuffer) null);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, this.iris$depthTextureId, 0);
        }
    }
}
