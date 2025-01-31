package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinLevelRenderer {
	@Inject(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;enableFog()V", ordinal = 0))
	private void iris$renderSky$beginNormalSky(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		// None of the vanilla sky is rendered until after this call, so if anything is rendered before, it's
		// CUSTOM_SKY.
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.SKY);
	}

	@Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;SUN:Lnet/minecraft/util/Identifier;"))
	private void iris$setSunRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.SUN);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getBackgroundColor(FF)[F"))
	private void iris$setSunsetRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.SUNSET);
	}

	@Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;MOON_PHASES:Lnet/minecraft/util/Identifier;"))
	private void iris$setMoonRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.MOON);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexBuffer;draw(I)V", ordinal = 1))
	private void iris$setStarRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;callList(I)V", ordinal = 1))
	private void iris$setStarRenderStage$Vbo(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ClientPlayerEntity;getCameraPosVec(F)Lnet/minecraft/util/math/Vec3d;"))
	private void iris$setVoidRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.VOID);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyAngle(F)F", ordinal = 1))
	private void iris$renderSky$tiltSun(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		GlStateManager.translate(0, 0, Iris.getPipelineManager().getPipelineNullable().getSunPathRotation());
	}

	@Inject(method = "renderSky", at = @At(value = "RETURN"))
	private void iris$endSky(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderClouds", at = @At(value = "HEAD"))
	private void iris$beginClouds(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.CLOUDS);
	}

	@Inject(method = "renderClouds", at = @At(value = "RETURN"))
	private void iris$endClouds(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.NONE);
	}


	@Inject(method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;)V", at = @At("HEAD"))
	private void iris$beginTerrainLayer(RenderLayer renderLayer, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.fromTerrainRenderType(WorldRenderingPhase.fromLayer(renderLayer)));
	}

	@Inject(method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;)V", at = @At("RETURN"))
	private void iris$endTerrainLayer(RenderLayer renderLayer, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.NONE);
	}


	@Inject(method = "renderWorldBorder", at = @At(value = "HEAD"))
	private void iris$beginWorldBorder(Entity entity, float tickDelta, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.WORLD_BORDER);
	}

	@Inject(method = "renderWorldBorder", at = @At(value = "RETURN"))
	private void iris$endWorldBorder(Entity entity, float tickDelta, CallbackInfo ci) {
		Iris.getPipelineManager().getPipelineNullable().setPhase(WorldRenderingPhase.NONE);
	}

}
