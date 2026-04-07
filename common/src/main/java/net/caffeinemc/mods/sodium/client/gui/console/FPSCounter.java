package net.caffeinemc.mods.sodium.client.gui.console;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.MinecraftClient;

public class FPSCounter {
    private static final int SAMPLE_SIZE = 700;
    private static final int FPS_UPDATE_INTERVAL = 100;  // need an update interval, or it'll be too hard to read with inconsistent fps
    private static final int AVG_UPDATE_INTERVAL = 700;

    public static final FPSCounter INSTANCE = new FPSCounter();

    private final long[] frameTimings = new long[SAMPLE_SIZE];

    private int frameIndex = 0;
    private long lastFrameTime = System.nanoTime();
    private boolean isFilled = false;

    private double currentFps = 0.0;
    private double avgFps = 0.0;
    private double avgFrameTime = 0.0;

    private int frameCounter = 0;
    private int avgFrameCounter = 0;

    public void render() {
        MinecraftClient minecraft = MinecraftClient.getInstance();

        if (!SodiumClientMod.options().advanced.fpsOverlay) return;

        long currentTime = System.nanoTime();
        long deltaTime = currentTime - lastFrameTime;
        lastFrameTime = currentTime;

        frameTimings[frameIndex] = deltaTime;
        frameIndex = (frameIndex + 1) % SAMPLE_SIZE;
        if (frameIndex == 0) {
            isFilled = true;
        }

        frameCounter++;
        avgFrameCounter++;

        if (frameCounter >= FPS_UPDATE_INTERVAL) {
            currentFps = MinecraftClient.getCurrentFps();
            frameCounter = 0;
        }

        if (avgFrameCounter >= AVG_UPDATE_INTERVAL) {
            avgFrameTime = getAvgFt();
            avgFps = 1_000_000_000.0 / avgFrameTime;
            avgFrameCounter = 0;
        }

        double minFps = getMinFps();
        double maxFps = getMaxFps();

        String finalStr = String.format(
                "FPS: %.0f (avg %.0f | min %.0f / max %.0f) | %.1f ms (avg %.2f)",
                currentFps,
                avgFps,
                minFps,
                maxFps,
                deltaTime / 1_000_000.0,
                avgFrameTime / 1_000_000.0
        );

        minecraft.textRenderer.drawWithShadow(finalStr, 2, 2, 0xFFFFFF);
    }

    private double getAvgFt() {
        long total = 0;
        int count = isFilled ? SAMPLE_SIZE : frameIndex;
        for (int i = 0; i < count; i++) {
            total += frameTimings[i];
        }
        return (double) total / count;
    }

    private double getMinFps() {
        long min = Long.MAX_VALUE;
        int count = isFilled ? SAMPLE_SIZE : frameIndex;

        for (int i = 0; i < count; i++) {
            if (frameTimings[i] < min) {
                min = frameTimings[i];
            }
        }

        return 1_000_000_000.0 / min;
    }

    private double getMaxFps() {
        long max = Long.MIN_VALUE;
        int count = isFilled ? SAMPLE_SIZE : frameIndex;

        for (int i = 0; i < count; i++) {
            if (frameTimings[i] > max) {
                max = frameTimings[i];
            }
        }

        return 1_000_000_000.0 / max;
    }
}
