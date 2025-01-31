package net.coderbot.iris.mixin.vertices.immediate;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.vertices.ImmediateState;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Uses a priority of 999 to apply before the main Iris mixins to draw entities before deferred runs.
@Mixin(value = GameRenderer.class, priority = 999)
public class MixinLevelRenderer {
	@Inject(method = "renderWorld(FJ)V", at = @At("HEAD"))
	private void iris$immediateStateBeginLevelRender(float tickDelta, long limitTime, CallbackInfo ci) {
		ImmediateState.isRenderingLevel = true;
	}

	@Inject(method = "renderWorld(FJ)V", at = @At("RETURN"))
	private void iris$immediateStateEndLevelRender(float tickDelta, long limitTime, CallbackInfo ci) {
		ImmediateState.isRenderingLevel = false;
	}
}
