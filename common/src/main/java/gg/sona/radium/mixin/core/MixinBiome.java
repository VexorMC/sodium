package gg.sona.radium.mixin.core;

import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Biome.class)
public class MixinBiome {
    @Shadow
    @Final
    private static Biome[] BIOMES;

    @Shadow
    @Final
    public static Biome OCEAN;

    @Shadow
    @Final
    private static Logger LOGGER;

    /**
     * @reason Fix out of bounds biome ID access
     * @author Lunasa
     */
    @Overwrite
    public static Biome getBiomeById(int id, Biome fallback) {
        if (id >= 0 && id < BIOMES.length) {
            Biome biome = BIOMES[id];
            return biome == null ? fallback : biome;
        } else {
            LOGGER.warn("Biome ID is out of bounds: " + id + ", defaulting to 0 (Ocean)");
            return OCEAN;
        }
    }
}
