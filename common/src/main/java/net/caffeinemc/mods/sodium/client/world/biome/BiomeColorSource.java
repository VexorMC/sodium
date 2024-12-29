package net.caffeinemc.mods.sodium.client.world.biome;

import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.world.biome.Biome;

public enum BiomeColorSource {
    GRASS(Biome::getGrassColor),
    FOLIAGE(Biome::getFoliageColor),
    WATER((biome, pos) -> biome.waterColor);

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
