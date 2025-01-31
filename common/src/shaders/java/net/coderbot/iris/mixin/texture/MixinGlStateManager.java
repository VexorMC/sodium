package net.coderbot.iris.mixin.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.texture.TextureInfoCache;
import net.coderbot.iris.texture.TextureTracker;
import net.coderbot.iris.texture.pbr.PBRTextureManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	@Inject(method = "bindTexture(I)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V", shift = Shift.AFTER, remap = false))
	private static void iris$onBindTexture(int id, CallbackInfo ci) {
		TextureTracker.INSTANCE.onBindTexture(id);
	}

	@Inject(method = "deleteTexture(I)V", at = @At("TAIL"))
	private static void iris$onDeleteTexture(int id, CallbackInfo ci) {
		iris$onDeleteTexture(id);
	}

	@Unique
	private static void iris$onDeleteTexture(int id) {
		TextureTracker.INSTANCE.onDeleteTexture(id);
		TextureInfoCache.INSTANCE.onDeleteTexture(id);
		PBRTextureManager.INSTANCE.onDeleteTexture(id);
	}
}
