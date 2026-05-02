package net.caffeinemc.mods.sodium.client.gui;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.QuadSplittingMode;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.caffeinemc.mods.sodium.client.util.FileUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import net.caffeinemc.mods.sodium.client.render.chunk.DeferMode;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

public class SodiumOptions {
    private static final String DEFAULT_FILE_NAME = "radium-options.json";

    public final QualitySettings quality = new QualitySettings();
    public final AdvancedSettings advanced = new AdvancedSettings();
    public final PerformanceSettings performance = new PerformanceSettings();
    public final NotificationSettings notifications = new NotificationSettings();
    public @NotNull DebugSettings debug = new DebugSettings();

    private boolean readOnly;

    private SodiumOptions() {
        // NO-OP
    }

    public static SodiumOptions defaults() {
        return new SodiumOptions();
    }

    public static class DebugSettings {
        public boolean terrainSortingEnabled = true;
    }

    public static class PerformanceSettings {
        public int chunkBuilderThreads = 0;
        public DeferMode chunkBuildDeferMode = DeferMode.ALWAYS;
        public boolean useAsyncCulling = false;

        //public boolean animateOnlyVisibleTextures = true;
        public boolean useEntityCulling = true;
        public boolean useFogOcclusion = true;
        public boolean useBlockFaceCulling = true;

        public boolean smartCull = false;

        public QuadSplittingMode quadSplittingMode = QuadSplittingMode.SAFE;
    }

    public static class AdvancedSettings {
        public boolean enableMemoryTracing = false;
        public boolean useAdvancedStagingBuffers = true;

        public boolean cpuRenderAhead = true;
        public int cpuRenderAheadLimit = 3;

        public boolean fpsOverlay = false;
    }

    public static class QualitySettings {
        public boolean enableClouds = false;
        public int cloudHeight = 160;
        public GraphicsQuality cloudQuality = GraphicsQuality.DEFAULT;
        public GraphicsQuality weatherQuality = GraphicsQuality.DEFAULT;
        public GraphicsQuality leavesQuality = GraphicsQuality.DEFAULT;
        public LightingQuality smoothLighting = LightingQuality.HIGH;

        public double chunkSectionFadeInTime = 750;

        public int biomeBlendRadius = 2;

        public boolean enableVignette = true;

        public boolean brightenFireworks = true;
        public boolean betterSkies = true;
    }

    public static class NotificationSettings {
        public boolean hasClearedDonationButton = false;
        public boolean hasSeenDonationPrompt = false;
    }

    public enum LightingQuality implements TextProvider {
        OFF(new TranslatableText("options.ao.off")),
        LOW(new TranslatableText("options.ao.min")),
        HIGH(new TranslatableText("options.ao.max"));

        private final Text name;

        LightingQuality(Text name) {
            this.name = name;
        }

        @Override
        public Text getLocalizedName() {
            return this.name;
        }
    }

    public enum GraphicsQuality implements TextProvider {
        DEFAULT("generator.default"),
        FANCY("options.graphics.fancy"),
        FAST("options.graphics.fast");

        private final Text name;

        GraphicsQuality(String name) {
            this.name = new TranslatableText(name);
        }

        @Override
        public Text getLocalizedName() {
            return this.name;
        }

        public boolean isFancy(boolean fancy) {
            return (this == FANCY) || (this == DEFAULT && fancy);
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    public static SodiumOptions loadFromDisk() {
        Path path = getConfigPath();
        SodiumOptions config;

        if (Files.exists(path)) {
            try (FileReader reader = new FileReader(path.toFile())) {
                config = GSON.fromJson(reader, SodiumOptions.class);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse config", e);
            }
        } else {
            config = new SodiumOptions();
        }

        try {
            writeToDisk(config);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't update config file", e);
        }

        return config;
    }

    private static Path getConfigPath() {
        return PlatformRuntimeInformation.getInstance().getConfigDirectory()
                .resolve(DEFAULT_FILE_NAME);
    }

    public static void writeToDisk(SodiumOptions config) throws IOException {
        if (config.isReadOnly()) {
            throw new IllegalStateException("Config file is read-only");
        }

        Path path = getConfigPath();
        Path dir = path.getParent();

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        } else if (!Files.isDirectory(dir)) {
            throw new IOException("Not a directory: " + dir);
        }

        FileUtil.writeTextRobustly(GSON.toJson(config), path);
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly() {
        this.readOnly = true;
    }
}
