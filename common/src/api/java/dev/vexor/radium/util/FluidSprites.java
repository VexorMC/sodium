package dev.vexor.radium.util;

import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;

import java.util.List;
import java.util.Objects;

/**
 * A utility class for managing and caching fluid sprites.
 * Allows quick access to water and lava sprites for maximum efficiency.
 *
 * Key Changes:
 * - Changed the sprites' storage type from `Sprite[]` to `List<Sprite>` for easier manipulation.
 * - Added utility methods to load and fetch fluid sprites from the sprite atlas.
 * - Ensured null safety in the constructor to prevent passing null values.
 * - Improved sprite fetching with specific error handling for missing sprites.
 * - Refactored the `getSprites` method to return a `Sprite[]` by converting the list to an array.
 *
 * @author Lunasa
 */
public class FluidSprites {
    // Constants for sprite suffixes and format
    private static final String STILL_SUFFIX = "_still";
    private static final String FLOW_SUFFIX = "_flow";
    private static final String SPRITE_PATH_FORMAT = "minecraft:blocks/%s%s";

    private final List<Sprite> waterSprites;
    private final List<Sprite> lavaSprites;

    // Constructor to initialize the sprite lists, ensuring they're not null
    private FluidSprites(List<Sprite> waterSprites, List<Sprite> lavaSprites) {
        this.waterSprites = Objects.requireNonNull(waterSprites, "Water sprites cannot be null");
        this.lavaSprites = Objects.requireNonNull(lavaSprites, "Lava sprites cannot be null");
    }

    /**
     * Gets the appropriate sprites for the given fluid block as an array.
     * This method returns a Sprite array based on the type of fluid (water or lava).
     *
     * @param fluidBlock The fluid block.
     * @return An array of sprites for the fluid.
     */
    public Sprite[] getSprites(AbstractFluidBlock fluidBlock) {
        // Return water or lava sprites as an array based on the material of the fluid block
        if (fluidBlock.getMaterial() == Material.WATER) {
            return waterSprites.toArray(new Sprite[0]); // Convert List to array for water
        }
        return lavaSprites.toArray(new Sprite[0]); // Convert List to array for lava
    }

    /**
     * Creates an instance of FluidSprites with preloaded sprites for water and lava.
     * This method loads the sprites for water and lava and returns a new instance of FluidSprites.
     *
     * @return A new FluidSprites instance with water and lava sprites loaded.
     */
    public static FluidSprites create() {
        return new FluidSprites(loadSprites("water"), loadSprites("lava"));
    }

    /**
     * Loads sprites for a given fluid name (water or lava).
     * This method fetches both the "still" and "flow" sprites for the specified fluid.
     *
     * @param fluidName The name of the fluid (e.g., "water", "lava").
     * @return A list containing the still and flow sprites for the fluid.
     */
    private static List<Sprite> loadSprites(String fluidName) {
        SpriteAtlasTexture atlas = MinecraftClient.getInstance().getSpriteAtlasTexture();

        // Fetch the still and flow sprites for the given fluid
        Sprite stillSprite = fetchSprite(atlas, fluidName, STILL_SUFFIX);
        Sprite flowSprite = fetchSprite(atlas, fluidName, FLOW_SUFFIX);

        return List.of(stillSprite, flowSprite);
    }

    /**
     * Fetches a single sprite from the atlas using the fluid name and suffix.
     * This method checks the sprite atlas for the sprite matching the given path.
     *
     * @param atlas     The sprite atlas texture.
     * @param fluidName The name of the fluid (e.g., "water", "lava").
     * @param suffix    The suffix for the sprite type (e.g., "_still" or "_flow").
     * @return The fetched sprite.
     * @throws IllegalStateException If the sprite cannot be found in the atlas.
     */
    private static Sprite fetchSprite(SpriteAtlasTexture atlas, String fluidName, String suffix) {
        // Construct the sprite path based on the fluid name and suffix
        String path = String.format(SPRITE_PATH_FORMAT, fluidName, suffix);

        // Fetch the sprite from the atlas using the constructed path
        Sprite sprite = atlas.getSprite(path);

        // Not Found?
        if (sprite == null) {
            throw new IllegalStateException("Sprite not found for path: " + path);
        }

        return sprite;
    }
}
