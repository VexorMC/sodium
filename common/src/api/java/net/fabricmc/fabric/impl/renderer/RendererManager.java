package net.fabricmc.fabric.impl.renderer;

import net.fabricmc.fabric.api.renderer.v1.Renderer;

public final class RendererManager {
    private static Renderer activeRenderer;

    private RendererManager() {
    }

    public static Renderer getRenderer() {
        if (activeRenderer == null) {
            throw new UnsupportedOperationException("Attempted to retrieve active rendering plug-in before one was registered.");
        }

        return activeRenderer;
    }

    public static void registerRenderer(Renderer renderer) {
        if (renderer == null) {
            throw new NullPointerException("Attempted to register a null rendering plug-in. This is not supported.");
        }

        if (activeRenderer != null) {
            throw new UnsupportedOperationException("Attempted to register a second rendering plug-in. Multiple rendering plug-ins are not supported.");
        }

        activeRenderer = renderer;
    }
}
