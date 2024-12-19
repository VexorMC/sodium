package net.caffeinemc.mods.sodium.client.world;

import net.minecraft.client.world.ClientWorld;

public interface BiomeSeedProvider {
    static long getBiomeZoomSeed(ClientWorld level) {
        return ((BiomeSeedProvider) level).sodium$getBiomeZoomSeed();
    }

    long sodium$getBiomeZoomSeed();
}
