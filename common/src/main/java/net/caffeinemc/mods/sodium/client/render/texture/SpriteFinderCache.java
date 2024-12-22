package net.caffeinemc.mods.sodium.client.render.texture;

import dev.vexor.radium.frapi.api.renderer.v1.model.SpriteFinder;
import net.minecraft.client.MinecraftClient;

/**
 * Caches {@link SpriteFinder}s for maximum efficiency. They must be refreshed after each resource reload.
 *
 * <p><b>This class should not be used during a resource reload</b>, as returned SpriteFinders may be null or outdated.
 */
public class SpriteFinderCache {
    private static SpriteFinder blockAtlasSpriteFinder;

    public static SpriteFinder forBlockAtlas() {
        if (blockAtlasSpriteFinder == null) {
            blockAtlasSpriteFinder = SpriteFinder.get(MinecraftClient.getInstance().getSpriteAtlasTexture());
        }

        return blockAtlasSpriteFinder;
    }

    public static void resetSpriteFinder() {
        blockAtlasSpriteFinder = null;
    }
}
