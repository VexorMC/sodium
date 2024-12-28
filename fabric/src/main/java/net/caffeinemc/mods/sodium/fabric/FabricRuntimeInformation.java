package net.caffeinemc.mods.sodium.fabric;

import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricRuntimeInformation implements PlatformRuntimeInformation {
    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
