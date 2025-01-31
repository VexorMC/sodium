package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GLX;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A simple optimization to avoid redundant glBindFramebuffer calls, works in principle the same as things like
 * glBindTexture in GlStateManager.
 */
@Mixin(GLX.class)
public class MixinGlStateManager_FramebufferBinding {

}
