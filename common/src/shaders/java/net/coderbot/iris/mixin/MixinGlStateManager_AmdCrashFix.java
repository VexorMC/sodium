package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.shader.ShaderWorkarounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Works around a bug in AMD drivers that causes crashes with glShaderSource.
 *
 * See {@link ShaderWorkarounds#safeShaderSource(int, CharSequence)} for more details.
 */
@Mixin(GLX.class)
public class MixinGlStateManager_AmdCrashFix {

}
