package net.caffeinemc.mods.sodium.fabric;

import dev.vexor.radium.Hooks;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.ornithemc.osl.lifecycle.api.client.ClientWorldEvents;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;

public class SodiumFabricMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModContainer mod = FabricLoader.getInstance()
                .getModContainer("radium")
                .orElseThrow(NullPointerException::new);

        SodiumClientMod.onInitialization(mod.getMetadata().getVersion().getFriendlyString());

        MinecraftClientEvents.TICK_START.register(c -> Hooks.CLIENT_TICK.forEach(Runnable::run));
        ClientWorldEvents.TICK_START.register(c -> Hooks.WORLD_TICK.forEach(Runnable::run));
    }
}
