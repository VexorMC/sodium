package dev.vexor.radium.mixin.core.mcpatcher.sky;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.prupe.mcpatcher.sky.SkyRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Shadow
    private ClientWorld world;

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

    @ModifyArg(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;translate(FFF)V", ordinal = 1),
            index = 1)
    private float modifyRenderSky8(float input) {
        // -((d0 - 16.0D)) turned into -((d0 - SkyRenderer.horizonHeight))
        return (float) (input - 16f + 16);
    }

}
