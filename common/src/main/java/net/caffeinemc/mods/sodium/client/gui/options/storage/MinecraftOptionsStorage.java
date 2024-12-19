package net.caffeinemc.mods.sodium.client.gui.options.storage;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

public class MinecraftOptionsStorage implements OptionStorage<GameOptions> {
    private final MinecraftClient minecraft;

    public MinecraftOptionsStorage() {
        this.minecraft = MinecraftClient.getInstance();
    }

    @Override
    public GameOptions getData() {
        return this.minecraft.options;
    }

    @Override
    public void save() {
        this.getData().save();

        SodiumClientMod.logger().info("Flushed changes to Minecraft configuration");
    }
}
