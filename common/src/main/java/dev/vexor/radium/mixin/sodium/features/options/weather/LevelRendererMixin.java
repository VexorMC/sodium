package dev.vexor.radium.mixin.sodium.features.options.weather;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class LevelRendererMixin {
    @Redirect(method = "renderWeather", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;fancyGraphics:Z"))
    private boolean redirectGetFancyWeather(GameOptions instance) {
        return SodiumClientMod.options().quality.weatherQuality.isFancy(instance.fancyGraphics);
    }

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getCloudMode()I"))
    private int redirectClouds(GameOptions instance) {
        return SodiumClientMod.options().quality.enableClouds ? 1 : 0;
    }
}