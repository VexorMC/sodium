package net.coderbot.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.platform.GLX;
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
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.resource.ResourceManager;
import net.coderbot.iris.vendored.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow
    private MinecraftClient client;
    @Unique
    private WorldRenderingPipeline pipeline;

    @Inject(method = "<init>", at = @At("TAIL"))
	private void iris$logSystem(MinecraftClient minecraftClient, ResourceManager resourceManager, CallbackInfo ci) {
		Iris.logger.info("Hardware information:");
		Iris.logger.info("CPU: " + GLX.getProcessor());
		Iris.logger.info("GPU: " + GL11.glGetString(7937) + " (Supports OpenGL " + GL11.glGetString(7938) + ")");
		Iris.logger.info("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")");
	}

	@Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(F)V"))
	private void disableVanillaHandRendering(HeldItemRenderer instance, float tickDelta) {
	}

    // Begin shader rendering after buffers have been cleared.
    // At this point we've ensured that Minecraft's main framebuffer is cleared.
    // This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
    // all pixels.
    @Inject(method = "renderWorld(FJ)V", at = @At("HEAD"))
    private void iris$beginLevelRender(float tickDelta, long limitTime, CallbackInfo ci) {
        if (Iris.isSodiumInvalid()) {
            throw new IllegalStateException("An invalid version of Sodium is installed, and the warning screen somehow" +
                    " didn't work. This is a bug! Please report it to the Iris developers.");
        }

        CapturedRenderingState.INSTANCE.setGbufferModelView(new Matrix4f(Camera.MODEL_MATRIX));
        CapturedRenderingState.INSTANCE.setGbufferProjection(new Matrix4f(Camera.PROJECTION_MATRIX));
        CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
        SystemTimeUniforms.COUNTER.beginFrame();
        SystemTimeUniforms.TIMER.beginFrame(limitTime);

        Program.unbind();

        pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());

        pipeline.beginLevelRendering();
    }

    @Inject(method = "renderWorld(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
    private void iris$endLevelRender(float partialTicks, long limitTime, CallbackInfo callback) {
        // TODO: Iris
        HandRenderer.INSTANCE.renderTranslucent(partialTicks, this.client.gameRenderer, pipeline);
        MinecraftClient.getInstance().profiler.swap("iris_final");
        pipeline.finalizeLevelRendering();
        Program.unbind();
    }

    @WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(F)V"))
    private void iris$disableVanillaRenderHand(HeldItemRenderer instance, float tickDelta, Operation<Void> original) {
        if (Iris.getCurrentPack().isEmpty()) {
            original.call(instance, tickDelta);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/render/CameraView;IZ)V", shift = At.Shift.AFTER), method = "renderWorld(IFJ)V")
    private void iris$beginEntities(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.renderShadows((LevelRendererAccessor) this.client.worldRenderer);
    }


    @Redirect(method = "renderWorld(IFJ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;viewDistance:I"))
    private int iris$alwaysRenderSky(GameOptions instance) {
        return Math.max(instance.viewDistance, 4);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderSky(FI)V"))
    private void iris$beginSky(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        // Use CUSTOM_SKY until levelFogColor is called as a heuristic to catch FabricSkyboxes.
        pipeline.setPhase(WorldRenderingPhase.CUSTOM_SKY);
    }


    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderSky(FI)V", shift = At.Shift.AFTER))
    private void iris$endSky(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.NONE);
    }

    @WrapOperation(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderClouds(Lnet/minecraft/client/render/WorldRenderer;FI)V"))
    private void iris$clouds(GameRenderer instance, WorldRenderer worldRenderer, float tickDelta, int anaglyphFilter, Operation<Void> original) {
        pipeline.setPhase(WorldRenderingPhase.CLOUDS);
        original.call(instance, worldRenderer, tickDelta, anaglyphFilter);
        pipeline.setPhase(WorldRenderingPhase.NONE);
    }


    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWeather(F)V"))
    private void iris$beginWeatherAndwriteRainAndSnowToDepthBuffer(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.RAIN_SNOW);
        if (pipeline.shouldWriteRainAndSnowToDepthBuffer()) {
            GL11.glDepthMask(true);
        }
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWeather(F)V", shift = At.Shift.AFTER))
    private void iris$endWeather(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.NONE);
    }
}
