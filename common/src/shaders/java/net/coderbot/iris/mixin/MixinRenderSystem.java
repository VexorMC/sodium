package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GLDebug;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GLX.class)
public class MixinRenderSystem {
	@Inject(method = "createContext", at = @At("RETURN"))
	private static void iris$onRendererInit(CallbackInfo ci) {
		GLDebug.initRenderer();
		IrisRenderSystem.initRenderer();
		Iris.onRenderSystemInit();
	}
}
