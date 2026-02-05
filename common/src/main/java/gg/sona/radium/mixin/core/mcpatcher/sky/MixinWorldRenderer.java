package gg.sona.radium.mixin.core.mcpatcher.sky;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gg.sona.radium.mcpatcher.sky.SkyRenderer;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Shadow
    private ClientWorld world;

    @Shadow
    private boolean vbo = false;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderSky", at = @At("HEAD"))
    private void modifyRenderSky1(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        SkyRenderer.setup(this.world, tickDelta, this.world.getSkyAngle(tickDelta));
    }

    @Inject(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;rotate(FFFF)V", ordinal = 4))
    private void modifyRenderSky3(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        SkyRenderer.renderAll();
    }

    // Ordinal 0 shouldn't be redirected unfortunately
    @ModifyArg(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/TextureManager;bindTexture(Lnet/minecraft/util/Identifier;)V",
            ordinal = 1))
    private Identifier modifyRenderSky4(Identifier location) {
        return SkyRenderer.setupCelestialObject(location);
    }

    @ModifyArg(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/TextureManager;bindTexture(Lnet/minecraft/util/Identifier;)V"))
    private Identifier modifyRenderSky5(Identifier location) {
        return SkyRenderer.setupCelestialObject(location);
    }

    @WrapWithCondition(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;color(FFFF)V", ordinal = 1))
    private boolean modifyRenderSky6(float f1, float f2, float f3, float f4) {
        return !SkyRenderer.active;
    }

    @WrapWithCondition(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;callList(I)V", ordinal = 1))
    private boolean modifyRenderSky7(int i) {
        return !SkyRenderer.active;
    }

    @Redirect(
            method = "renderSky(FI)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/render/WorldRenderer;vbo:Z"
            )
    )
    private boolean patcher$fixVBO(WorldRenderer instance) {
        return !SodiumClientMod.options().quality.betterSkies && this.vbo;
    }

    @ModifyArg(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;translate(FFF)V", ordinal = 1),
            index = 1)
    private float modifyRenderSky8(float input) {
        // -((d0 - 16.0D)) turned into -((d0 - SkyRenderer.horizonHeight))
        return -((float)(input - (double)256));
    }
}
