package net.coderbot.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinLevelRenderer {
    @Shadow
    @Final
    private MinecraftClient client;
    @Unique
    private WorldRenderingPipeline pipeline;

    @Unique
    private void iris$beginTranslucents(WorldRenderingPipeline pipeline) {
        pipeline.beginHand();
        HandRenderer.INSTANCE.renderSolid(CapturedRenderingState.INSTANCE.getTickDelta(), this.client.gameRenderer, pipeline);
        this.client.profiler.swap("iris_pre_translucent");
        pipeline.beginTranslucents();
    }

    @Inject(method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;)V", at = @At("HEAD"))
    private void iris$beginRenderLayer(RenderLayer renderLayer, CallbackInfo callbackInfo) {
        if(renderLayer == RenderLayer.TRANSLUCENT) {
            if (!ShadowRenderer.ACTIVE) {
                iris$beginTranslucents(pipeline);
            }

            pipeline.setPhase(WorldRenderingPhase.TERRAIN_TRANSLUCENT);

            this.client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
        } else if (renderLayer == RenderLayer.CUTOUT) {
            pipeline.setPhase(WorldRenderingPhase.TERRAIN_CUTOUT);
        } else if (renderLayer == RenderLayer.CUTOUT_MIPPED) {
            pipeline.setPhase(WorldRenderingPhase.TERRAIN_CUTOUT_MIPPED);
        } else if (renderLayer == RenderLayer.SOLID) {
            pipeline.setPhase(WorldRenderingPhase.TERRAIN_SOLID);
        }
    }

    @Inject(method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;)V", at = @At("TAIL"))
    private void iris$endRenderLayer(RenderLayer renderLayer, CallbackInfo callbackInfo) {
        pipeline.setPhase(WorldRenderingPhase.NONE);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Tessellator;getBuffer()Lnet/minecraft/client/render/BufferBuilder;"))
    private void iris$renderSky$beginNormalSky(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        // None of the vanilla sky is rendered until after this call, so if anything is rendered before, it's CUSTOM_SKY.
        pipeline = Iris.getPipelineManager().getPipelineNullable();

        pipeline.setPhase(WorldRenderingPhase.SKY);
    }

    @Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;SUN:Lnet/minecraft/util/Identifier;"))
    private void iris$setSunRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.SUN);
    }

    @Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;MOON_PHASES:Lnet/minecraft/util/Identifier;"))
    private void iris$setMoonRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.MOON);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getBackgroundColor(FF)[F"))
    private void iris$setSunsetRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.SUNSET);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;method_3707(F)F"))
    private void iris$setStarRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.STARS);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ClientPlayerEntity;getCameraPosVec(F)Lnet/minecraft/util/math/Vec3d;"))
    private void iris$setVoidRenderStage(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        pipeline.setPhase(WorldRenderingPhase.VOID);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyAngle(F)F"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F")))
    private void iris$renderSky$tiltSun(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        GL11.glRotatef(pipeline.getSunPathRotation(), 0.0F, 0.0F, 1.0F);
    }

    @WrapOperation(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;renderEntity(Lnet/minecraft/entity/Entity;F)Z"))
    private boolean iris$renderEntities$capture(EntityRenderDispatcher instance, Entity entity, float f, Operation<Boolean> original) {
        CapturedRenderingState.INSTANCE.setCurrentEntity(entity.getEntityId());
        GbufferPrograms.beginEntities();
        try {
            return original.call(instance, entity, f);
        } finally {
            CapturedRenderingState.INSTANCE.setCurrentEntity(-1);
            GbufferPrograms.endEntities();
        }
    }
}
