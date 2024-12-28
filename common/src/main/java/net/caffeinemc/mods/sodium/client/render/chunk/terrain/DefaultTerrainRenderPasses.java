package net.caffeinemc.mods.sodium.client.render.chunk.terrain;

import net.minecraft.client.render.RenderLayer;

public class DefaultTerrainRenderPasses {
    public static final TerrainRenderPass SOLID = new TerrainRenderPass(RenderLayer.SOLID, false, false);
    public static final TerrainRenderPass CUTOUT = new TerrainRenderPass(RenderLayer.CUTOUT, false, true);
    public static final TerrainRenderPass CUTOUT_MIPPED = new TerrainRenderPass(RenderLayer.CUTOUT_MIPPED, false, true);
    public static final TerrainRenderPass TRANSLUCENT = new TerrainRenderPass(RenderLayer.TRANSLUCENT, true, false);

    public static final TerrainRenderPass[] ALL = new TerrainRenderPass[] { SOLID, CUTOUT, CUTOUT_MIPPED, TRANSLUCENT };

    public static TerrainRenderPass fromLayer(RenderLayer layer) {
        return switch (layer) {
            case SOLID -> SOLID;
            case CUTOUT -> CUTOUT;
            case CUTOUT_MIPPED -> CUTOUT_MIPPED;
            case TRANSLUCENT -> TRANSLUCENT;
        };
    }
}
