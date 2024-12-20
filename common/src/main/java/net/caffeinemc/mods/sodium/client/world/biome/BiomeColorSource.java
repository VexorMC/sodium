package net.caffeinemc.mods.sodium.client.world.biome;

import net.minecraft.client.color.world.BiomeColors;

import static net.caffeinemc.mods.sodium.client.world.biome.BiomeColorMaps.*;

public enum BiomeColorSource {
    GRASS(GRASS_COLOR),
    FOLIAGE(FOLIAGE_COLOR),
    WATER(WATER_COLOR);

    private final BiomeColors.ColorProvider provider;

    BiomeColorSource(BiomeColors.ColorProvider provider) {
        this.provider = provider;
    }

    public BiomeColors.ColorProvider getProvider() {
        return provider;
    }

    public static final BiomeColorSource[] VALUES = BiomeColorSource.values();
    public static final int COUNT = VALUES.length;
}
