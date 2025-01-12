package dev.vexor.radium.mixin.sodium.features.options.overlays;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public class GuiMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isFancyGraphicsEnabled()Z"))
    private boolean redirectFancyGraphicsVignette() {
        return SodiumClientMod.options().quality.enableVignette;
    }
}
