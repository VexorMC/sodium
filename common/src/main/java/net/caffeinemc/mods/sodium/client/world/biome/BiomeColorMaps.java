package net.caffeinemc.mods.sodium.client.world.biome;

import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.world.biome.Biome;

public class BiomeColorMaps {
    public static final BiomeColors.ColorProvider GRASS_COLOR = Biome::getGrassColor;
    public static final BiomeColors.ColorProvider FOLIAGE_COLOR = Biome::getFoliageColor;
    public static final BiomeColors.ColorProvider WATER_COLOR = (biome, pos) -> biome.waterColor;

    private static final int WIDTH = 256;
    private static final int HEIGHT = 256;

    private static final int INVALID_INDEX = -1;

    public static int getGrassColor(int index) {
        if (index == INVALID_INDEX || index >= GrassColors.colorMap.length) {
            return GrassColors.getColor(0, 0);
        }

        return GrassColors.colorMap[index];
    }

    public static int getFoliageColor(int index) {
        if (index == INVALID_INDEX || index >= FoliageColors.colorMap.length) {
            return FoliageColors.getDefaultColor();
        }

        return FoliageColors.colorMap[index];
    }

    public static int getIndex(double temperature, double humidity) {
        humidity *= temperature;

        int x = (int) ((1.0D - temperature) * 255.0D);
        int y = (int) ((1.0D - humidity) * 255.0D);

        if (x < 0 || x >= WIDTH) {
            return INVALID_INDEX;
        }

        if (y < 0 || y >= HEIGHT) {
            return INVALID_INDEX;
        }

        return (y << 8) | x;
    }
}
