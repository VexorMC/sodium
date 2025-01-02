package net.irisshaders.iris.uniforms;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.irisshaders.iris.gl.uniform.FloatSupplier;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.mixinterface.ExtendedBiome;
import net.irisshaders.iris.parsing.BiomeCategories;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.biome.Biome;

import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public class BiomeUniforms {
	private static final Object2IntMap<Biome> biomeMap = new Object2IntOpenHashMap<>();

	public static Object2IntMap<Biome> getBiomeMap() {
		return biomeMap;
	}

	public static void addBiomeUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1i(PER_TICK, "biome", playerI(player ->
				biomeMap.getInt(player.world.getBiome(player.getBlockPos()))))
			.uniform1i(PER_TICK, "biome_category", playerI(player -> {
				Biome holder = player.world.getBiome(player.getBlockPos());
				ExtendedBiome extendedBiome = ((ExtendedBiome) (Object) holder);
				if (extendedBiome.getBiomeCategory() == -1) {
					extendedBiome.setBiomeCategory(getBiomeCategory(holder).ordinal());
					return extendedBiome.getBiomeCategory();
				} else {
					return extendedBiome.getBiomeCategory();
				}
			}))
			.uniform1i(PER_TICK, "biome_precipitation", playerI(player -> (int) player.world.getBiome(player.getBlockPos()).getRainfall()))
			.uniform1f(PER_TICK, "rainfall", playerF(player ->
				((ExtendedBiome) (Object) player.world.getBiome(player.getBlockPos())).getDownfall()))
			.uniform1f(PER_TICK, "temperature", playerF(player ->
				player.world.getBiome(player.getBlockPos()).temperature));
	}

    private static BiomeCategories getBiomeCategory(Biome biome) {
        if (biome == Biome.ICE_PLAINS) {
            return BiomeCategories.ICY;
        } else if (biome == Biome.EXTREME_HILLS) {
            return BiomeCategories.EXTREME_HILLS;
        } else if (biome == Biome.TAIGA) {
            return BiomeCategories.TAIGA;
        } else if (biome == Biome.OCEAN) {
            return BiomeCategories.OCEAN;
        } else if (biome == Biome.JUNGLE) {
            return BiomeCategories.JUNGLE;
        } else if (biome == Biome.FOREST) {
            return BiomeCategories.FOREST;
        } else if (biome == Biome.MESA) {
            return BiomeCategories.MESA;
        }else if (biome == Biome.BEACH) {
            return BiomeCategories.BEACH;
        } else if (biome == Biome.DESERT) {
            return BiomeCategories.DESERT;
        } else if (biome == Biome.RIVER) {
            return BiomeCategories.RIVER;
        } else if (biome == Biome.SWAMPLAND) {
            return BiomeCategories.SWAMP;
        } else if (biome == Biome.ICE_MOUNTAINS) {
            return BiomeCategories.MOUNTAIN;
        } else {
            return BiomeCategories.PLAINS;
        }
    }

	static IntSupplier playerI(ToIntFunction<ClientPlayerEntity> function) {
		return () -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player == null) {
				return 0; // TODO: I'm not sure what I'm supposed to do here?
			} else {
				return function.applyAsInt(player);
			}
		};
	}

	static FloatSupplier playerF(ToFloatFunction<ClientPlayerEntity> function) {
		return () -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player == null) {
				return 0.0f; // TODO: I'm not sure what I'm supposed to do here?
			} else {
				return function.applyAsFloat(player);
			}
		};
	}

	@FunctionalInterface
	public interface ToFloatFunction<T> {
		/**
		 * Applies this function to the given argument.
		 *
		 * @param value the function argument
		 * @return the function result
		 */
		float applyAsFloat(T value);
	}
}
