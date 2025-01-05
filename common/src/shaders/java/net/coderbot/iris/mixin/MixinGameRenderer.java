package net.coderbot.iris.mixin;

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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
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

    @Unique
    private WorldRenderingPipeline pipeline;

    // Begin shader rendering after buffers have been cleared.
    // At this point we've ensured that Minecraft's main framebuffer is cleared.
    // This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
    // all pixels.
    @Inject(method = "renderWorld(IFJ)V", at = @At("HEAD"))
    private void iris$beginLevelRender(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        if (Iris.isSodiumInvalid()) {
            throw new IllegalStateException("An invalid version of Sodium is installed, and the warning screen somehow" +
                    " didn't work. This is a bug! Please report it to the Iris developers.");
        }

        CapturedRenderingState.INSTANCE.setGbufferModelView(new PoseStack().last().pose());
        CapturedRenderingState.INSTANCE.setGbufferProjection(new Matrix4f(Camera.PROJECTION_MATRIX));
        CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
        SystemTimeUniforms.COUNTER.beginFrame();
        SystemTimeUniforms.TIMER.beginFrame(limitTime);

        Program.unbind();

        pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());

        pipeline.beginLevelRendering();
    }

    // Inject a bit early so that we can end our rendering before mods like VoxelMap (which inject at RETURN)
    // render their waypoint beams.
    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void iris$endLevelRender(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        HandRenderer.INSTANCE.renderTranslucent(tickDelta, (GameRenderer) (Object) this, pipeline);
        MinecraftClient.getInstance().profiler.swap("iris_final");
        pipeline.finalizeLevelRendering();
        pipeline = null;
        Program.unbind();
    }

    // Setup shadow terrain & render shadows before the main terrain setup. We need to do things in this order to
    // avoid breaking other mods such as Light Overlay: https://github.com/IrisShaders/Iris/issues/1356
    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/render/CameraView;IZ)V"))
    private void iris$renderTerrainShadows(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.renderShadows((LevelRendererAccessor) this);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderSky(FI)V", shift = At.Shift.BEFORE))
    private void iris$beginSky(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        // Use CUSTOM_SKY until levelFogColor is called as a heuristic to catch FabricSkyboxes.
        pipeline.setPhase(WorldRenderingPhase.CUSTOM_SKY);
    }

    @Redirect(method = "renderWorld(IFJ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;viewDistance:I"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;clear(I)V")))
    private int iris$alwaysRenderSky(GameOptions options) {
        return Math.max(options.viewDistance, 4);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderSky(FI)V", shift = At.Shift.AFTER))
    private void iris$endSky(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.NONE);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderClouds(Lnet/minecraft/client/render/WorldRenderer;FI)V", shift = At.Shift.BEFORE))
    private void iris$beginClouds(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.CLOUDS);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderClouds(Lnet/minecraft/client/render/WorldRenderer;FI)V", shift = At.Shift.AFTER))
    private void iris$endClouds(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.NONE);
    }


    @Redirect(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;DILnet/minecraft/entity/Entity;)I"))
    private int iris$beginTerrainLayer(WorldRenderer instance, RenderLayer renderLayer, double tickDelta, int anaglyphFilter, Entity entity) {
        pipeline.setPhase(WorldRenderingPhase.fromTerrainRenderType(WorldRenderingPhase.fromLayer(renderLayer)));
        instance.renderLayer(renderLayer, tickDelta, anaglyphFilter, entity);
        pipeline.setPhase(WorldRenderingPhase.NONE);
        return anaglyphFilter;
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWeather(F)V", shift = At.Shift.BEFORE))
    private void iris$beginWeather(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.RAIN_SNOW);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWeather(F)V", shift = At.Shift.AFTER))
    private void iris$endWeather(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.NONE);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/entity/Entity;F)V", shift = At.Shift.BEFORE))
    private void iris$beginWorldBorder(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.WORLD_BORDER);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/entity/Entity;F)V", shift = At.Shift.AFTER))
    private void iris$endWorldBorder(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.NONE);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderDebugCrosshair(F)V"))
    private void iris$setDebugRenderStage(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.DEBUG);
    }

    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderDebugCrosshair(F)V", shift = At.Shift.AFTER))
    private void iris$resetDebugRenderStage(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.NONE);
    }


    @Inject(method = "renderWorld(IFJ)V", at = @At(value = "CONSTANT", args = "stringValue=translucent"))
    private void iris$beginTranslucents(int anaglyphFilter, float tickDelta, long limitTime, CallbackInfo ci) {
        pipeline.beginHand();
        HandRenderer.INSTANCE.renderSolid(tickDelta, (GameRenderer) (Object) this, pipeline);
        MinecraftClient.getInstance().profiler.swap("iris_pre_translucent");
        pipeline.beginTranslucents();
    }
}
