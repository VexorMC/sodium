package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
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
		pipeline.setPhase(WorldRenderingPhase.SKY);
	}

	@Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;SUN:Lnet/minecraft/util/Identifier;"))
	private void iris$setSunRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.SUN);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getBackgroundColor(FF)[F"))
	private void iris$setSunsetRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.SUNSET);
	}

	@Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;MOON_PHASES:Lnet/minecraft/util/Identifier;"))
	private void iris$setMoonRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.MOON);
	}

	@Inject(method = "renderStars(Lnet/minecraft/client/render/BufferBuilder;)V", at = @At(value = "HEAD"))
	private void iris$setStarRenderStage(BufferBuilder buffer, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = "renderDarkSky", at = @At(value = "HEAD"))
	private void iris$setVoidRenderStage(CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.VOID);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyAngle(F)F"),
		slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;rotate(FFFF)V")))
	private void iris$renderSky$tiltSun(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        GlStateManager.rotate(pipeline.getSunPathRotation(), 1.0F, 0.0F, 0.0F);
	}
}
