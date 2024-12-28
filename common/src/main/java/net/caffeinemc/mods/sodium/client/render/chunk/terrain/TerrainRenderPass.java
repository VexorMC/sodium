package net.caffeinemc.mods.sodium.client.render.chunk.terrain;

public class TerrainRenderPass {
    private final boolean isTranslucent;
    private final boolean fragmentDiscard;

    public TerrainRenderPass(boolean isTranslucent, boolean allowFragmentDiscard) {
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
