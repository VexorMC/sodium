package net.caffeinemc.mods.sodium.client.render.chunk.terrain;

import net.minecraft.client.render.RenderLayer;

public class TerrainRenderPass {
    @Deprecated(forRemoval = true)
    private final RenderLayer renderType;

    private final boolean isTranslucent;
    private final boolean fragmentDiscard;

    public TerrainRenderPass(RenderLayer renderType, boolean isTranslucent, boolean allowFragmentDiscard) {
        this.renderType = renderType;

        this.isTranslucent = isTranslucent;
        this.fragmentDiscard = allowFragmentDiscard;
    }

    public boolean isTranslucent() {
        return this.isTranslucent;
    }

    @Deprecated
    public void startDrawing() {
    }

    @Deprecated
    public void endDrawing() {
    }

    public boolean supportsFragmentDiscard() {
        return this.fragmentDiscard;
    }
}
