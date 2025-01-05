package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinLevelRenderer {
    @Unique
    private WorldRenderingPipeline pipeline;

    @Inject(method = "setupTerrain", at = @At("HEAD"))
    void iris$setupTerrain(CallbackInfo callbackInfo) {
        pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
    }

    @Inject(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;enableFog()V"))
	private void iris$renderSky$beginNormalSky(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		// None of the vanilla sky is rendered until after this call, so if anything is rendered before, it's
		// CUSTOM_SKY.
		if (pipeline != null) pipeline.setPhase(WorldRenderingPhase.SKY);
	}

	@Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;SUN:Lnet/minecraft/util/Identifier;"))
	private void iris$setSunRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		if (pipeline != null) pipeline.setPhase(WorldRenderingPhase.SUN);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getBackgroundColor(FF)[F"))
	private void iris$setSunsetRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		if (pipeline != null) pipeline.setPhase(WorldRenderingPhase.SUNSET);
	}

	@Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;MOON_PHASES:Lnet/minecraft/util/Identifier;"))
	private void iris$setMoonRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		if (pipeline != null) pipeline.setPhase(WorldRenderingPhase.MOON);
	}

	@Inject(method = "renderStars(Lnet/minecraft/client/render/BufferBuilder;)V", at = @At(value = "HEAD"))
	private void iris$setStarRenderStage(BufferBuilder buffer, CallbackInfo ci) {
		if (pipeline != null) pipeline.setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = "renderDarkSky", at = @At(value = "HEAD"))
	private void iris$setVoidRenderStage(CallbackInfo ci) {
		if (pipeline != null) pipeline.setPhase(WorldRenderingPhase.VOID);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyAngle(F)F"),
		slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;rotate(FFFF)V")))
	private void iris$renderSky$tiltSun(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        if (pipeline != null) GlStateManager.rotate(pipeline.getSunPathRotation(), 1.0F, 0.0F, 0.0F);
	}
}
