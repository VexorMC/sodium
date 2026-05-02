package net.caffeinemc.mods.sodium.client.gui;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.option.Range;
import net.caffeinemc.mods.sodium.api.config.structure.*;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.arena.staging.MappedStagingBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls;
import net.caffeinemc.mods.sodium.client.gui.options.named.GraphicsMode;
import net.caffeinemc.mods.sodium.client.render.chunk.DeferMode;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.QuadSplittingMode;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.io.IOException;

// TODO: get initialValue from the vanilla options (it's private)
public class SodiumConfigBuilder implements ConfigEntryPoint {
    private static final Identifier SODIUM_ICON = new Identifier("radium", "textures/gui/icon.png");
    private static final SodiumOptions DEFAULTS = SodiumOptions.defaults();

    private final GameOptions vanillaOpts;
    private final StorageEventHandler vanillaStorage;
    private final SodiumOptions sodiumOpts;
    private final StorageEventHandler sodiumStorage;

    public SodiumConfigBuilder() {
        var minecraft = MinecraftClient.getInstance();
        this.vanillaOpts = minecraft.options;
        this.vanillaStorage = this.vanillaOpts == null ? null : () -> {
            this.vanillaOpts.save();

            SodiumClientMod.logger().info("Flushed changes to Minecraft configuration");
        };

        this.sodiumOpts = SodiumClientMod.options();
        this.sodiumStorage = () -> {
            try {
                SodiumOptions.writeToDisk(this.sodiumOpts);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't save configuration changes", e);
            }

            SodiumClientMod.logger().info("Flushed changes to Sodium configuration");
        };
    }

    @Override
    public void registerConfigEarly(ConfigBuilder builder) {
        new SodiumConfigBuilder().buildEarlyConfig(builder);
    }

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        new SodiumConfigBuilder().buildFullConfig(builder);
    }

    private static ModOptionsBuilder createModOptionsBuilder(ConfigBuilder builder) {
        return builder.registerOwnModOptions()
                .setName("Radium")
                .setIcon(SODIUM_ICON)
                .formatVersion(version -> {
                    var result = version.split("\\+", 2);
                    return result[0];
                });
    }

    private void buildEarlyConfig(ConfigBuilder builder) {
        createModOptionsBuilder(builder).addPage(
                builder.createOptionPage()
                        .setName(new TranslatableText("sodium.options.pages.performance"))
                        .addOptionGroup(
                                builder.createOptionGroup()
                                        .addOption(this.buildNoErrorContextOption(builder))));
    }
    private OptionBuilder buildNoErrorContextOption(ConfigBuilder builder) {
        return builder.createBooleanOption(new Identifier("sodium:performance.use_no_error_context"))
                .setStorageHandler(this.sodiumStorage)
                .setName(new LiteralText("sodium.options.use_no_error_context.name"))
                .setTooltip(new LiteralText("sodium.options.use_no_error_context.tooltip"))
                .setDefaultValue(false)
                .setBinding(value -> { throw new IllegalStateException(); }, () -> false)
                .setEnabledProvider(state -> false)
                .setImpact(OptionImpact.LOW)
                .setFlags(OptionFlag.REQUIRES_GAME_RESTART);
    }
    private void buildFullConfig(ConfigBuilder builder) {
        createModOptionsBuilder(builder)
                .setColorTheme(builder.createColorTheme().setFullThemeRGB(
                        Colors.THEME, Colors.THEME_LIGHTER, Colors.THEME_DARKER))
                .addPage(this.buildGeneralPage(builder))
                .addPage(this.buildQualityPage(builder))
                .addPage(this.buildPerformancePage(builder))
                .addPage(this.buildAdvancedPage(builder));
    }

    private OptionPageBuilder buildGeneralPage(ConfigBuilder builder) {
        var generalPage = builder.createOptionPage().setName(new TranslatableText("stat.generalButton"));

        generalPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:general.render_distance"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.renderDistance"))
                                .setTooltip(new TranslatableText("sodium.options.view_distance.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.translateVariable("sodium.options.view_distance.value"))
                                .setRange(2, 32, 1)
                                .setDefaultValue(12)
                                .setBinding((value) -> this.vanillaOpts.viewDistance = value, () -> this.vanillaOpts.viewDistance)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:general.gamma"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.gamma"))
                                .setTooltip(new TranslatableText("sodium.options.brightness.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.brightness())
                                .setRange(0, 100, 1)
                                .setDefaultValue(50)
                                .setBinding(value -> this.vanillaOpts.gamma = (value * 0.01f), () -> (int) (this.vanillaOpts.gamma / 0.01D))
                )
        );

        generalPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:general.gui_scale"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.guiScale"))
                                .setTooltip(new TranslatableText("sodium.options.gui_scale.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.guiScale())
                                .setRangeProvider((state) -> new Range(0, 3, 1), ConfigState.UPDATE_ON_REBUILD)
                                .setDefaultValue(0)
                                .setBinding(value -> {
                                    this.vanillaOpts.guiScale = value;

                                    // Resizing our window
                                    if(MinecraftClient.getInstance().currentScreen instanceof VideoSettingsScreen) {
                                        MinecraftClient.getInstance().setScreen(VideoSettingsScreen.createScreen(((VideoSettingsScreen) MinecraftClient.getInstance().currentScreen).prevScreen));
                                    }
                                }, () -> this.vanillaOpts.guiScale)
                )
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:general.fullscreen"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.fullscreen"))
                                .setTooltip(new TranslatableText("sodium.options.fullscreen.tooltip"))
                                .setDefaultValue(false)
                                .setBinding(value -> {
                                    this.vanillaOpts.fullscreen = (value);

                                    MinecraftClient client = MinecraftClient.getInstance();

                                    if (client.isFullscreen() !=
                                            this.vanillaOpts.fullscreen) {
                                        client.toggleFullscreen();

                                        // The client might not be able to enter full-screen mode
                                        this.vanillaOpts.fullscreen = (client.isFullscreen());
                                    }
                                }, () -> this.vanillaOpts.fullscreen)
                )
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:general.vsync"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.vsync"))
                                .setTooltip(new TranslatableText("sodium.options.v_sync.tooltip"))
                                .setDefaultValue(true)
                                .setBinding(
                                        (value) -> {
                                            this.vanillaOpts.vsync = value;
                                            Display.setVSyncEnabled(value);
                                        },
                                        () -> this.vanillaOpts.vsync)
                                .setImpact(OptionImpact.VARIES)
                )
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:general.framerate_limit"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.framerateLimit"))
                                .setTooltip(new TranslatableText("sodium.options.fps_limit.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.fpsLimit())
                                .setRange(10, 260, 10)
                                .setDefaultValue(60)
                                .setBinding((value) -> this.vanillaOpts.maxFramerate = value, () -> this.vanillaOpts.maxFramerate)
                )
        );

        generalPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:general.view_bobbing"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.viewBobbing"))
                                .setTooltip(new TranslatableText("sodium.options.view_bobbing.tooltip"))
                                .setDefaultValue(true)
                                .setBinding((value) -> this.vanillaOpts.bobView = value, () -> this.vanillaOpts.bobView)
                )
        );

        return generalPage;
    }

    private OptionPageBuilder buildQualityPage(ConfigBuilder builder) {
        var qualityPage = builder.createOptionPage().setName(new TranslatableText("sodium.options.pages.quality"));

        qualityPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createEnumOption(new Identifier("radium:quality.graphics"), GraphicsMode.class)
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.graphics"))
                                .setTooltip(new TranslatableText("sodium.options.graphics_quality.tooltip"))
                                .setElementNameProvider(EnumOptionBuilder.nameProviderFrom(
                                        new TranslatableText("options.graphics.fast"),
                                        new TranslatableText("options.graphics.fancy")))
                                .setDefaultValue(GraphicsMode.FANCY)
                                .setBinding(
                                        (value) -> this.vanillaOpts.fancyGraphics = value.isFancy(),
                                        () -> GraphicsMode.fromBoolean(this.vanillaOpts.fancyGraphics))
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
        );

        qualityPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:quality.clouds"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("options.renderClouds"))
                                .setTooltip(new TranslatableText("sodium.options.clouds_quality.tooltip"))
                                .setDefaultValue(DEFAULTS.quality.enableClouds)
                                .setBinding(value -> this.sodiumOpts.quality.enableClouds = value, () -> this.sodiumOpts.quality.enableClouds)
                                .setImpact(OptionImpact.LOW)
                )
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:quality.cloud_height"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.cloud_height.name"))
                                .setTooltip(new TranslatableText("sodium.options.cloud_height.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.number())
                                .setRange(128, 230, 1)
                                .setDefaultValue(DEFAULTS.quality.cloudHeight)
                                .setBinding(value -> this.sodiumOpts.quality.cloudHeight = value, () -> this.sodiumOpts.quality.cloudHeight)
                                .setImpact(OptionImpact.LOW)
                )
        );

        qualityPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createEnumOption(new Identifier("radium:quality.smooth_lighting"), SodiumOptions.LightingQuality.class)
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("options.ao"))
                                .setTooltip(new TranslatableText("sodium.options.smooth_lighting.tooltip"))
                                .setDefaultValue(DEFAULTS.quality.smoothLighting)
                                .setBinding(value -> this.sodiumOpts.quality.smoothLighting = value, () -> this.sodiumOpts.quality.smoothLighting)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createEnumOption(new Identifier("radium:quality.weather_quality"), SodiumOptions.GraphicsQuality.class)
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.weather_quality.name"))
                                .setTooltip(new TranslatableText("sodium.options.weather_quality.tooltip"))
                                .setDefaultValue(DEFAULTS.quality.weatherQuality)
                                .setBinding(value -> this.sodiumOpts.quality.weatherQuality = value, () -> this.sodiumOpts.quality.weatherQuality)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createEnumOption(new Identifier("radium:quality.leaves_quality"), SodiumOptions.GraphicsQuality.class)
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.leaves_quality.name"))
                                .setTooltip(new TranslatableText("sodium.options.leaves_quality.tooltip"))
                                .setDefaultValue(DEFAULTS.quality.leavesQuality)
                                .setBinding(value -> this.sodiumOpts.quality.leavesQuality = value, () -> this.sodiumOpts.quality.leavesQuality)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:quality.biome_blend"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.biome_blend.name"))
                                .setTooltip(new TranslatableText("sodium.options.biome_blend.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.biomeBlend())
                                .setRange(0, 7, 1)
                                .setDefaultValue(DEFAULTS.quality.biomeBlendRadius)
                                .setBinding(value -> this.sodiumOpts.quality.biomeBlendRadius = value, () -> this.sodiumOpts.quality.biomeBlendRadius)
                                .setImpact(OptionImpact.LOW)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:quality.entity_shadows"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.entityShadows"))
                                .setTooltip(new TranslatableText("sodium.options.entity_shadows.tooltip"))
                                .setDefaultValue(true)
                                .setBinding(value -> this.vanillaOpts.entityShadows = value, () -> this.vanillaOpts.entityShadows)
                                .setImpact(OptionImpact.MEDIUM)
                )
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:quality.vignette"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.vignette.name"))
                                .setTooltip(new TranslatableText("sodium.options.vignette.tooltip"))
                                .setDefaultValue(DEFAULTS.quality.enableVignette)
                                .setBinding(value -> this.sodiumOpts.quality.enableVignette = value, () -> this.sodiumOpts.quality.enableVignette)
                )
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:quality.fade_time"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.chunkFade.name"))
                                .setTooltip(new TranslatableText("sodium.options.chunkFade.tooltip"))
                                .setDefaultValue(750)
                                .setValueFormatter(ControlValueFormatterImpls.chunkFade())
                                .setRange(new Range(0, 2000, 50))
                                .setBinding(fade -> this.sodiumOpts.quality.chunkSectionFadeInTime = ((double) fade / 1000.0), () -> (int) (this.sodiumOpts.quality.chunkSectionFadeInTime * 1000.0))
                )
        );

        qualityPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:quality.mipmap_levels"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.mipmapLevels"))
                                .setTooltip(new TranslatableText("sodium.options.mipmap_levels.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.multiplier())
                                .setRange(0, 4, 1)
                                .setDefaultValue(4)
                                .setBinding(value -> this.vanillaOpts.mipmapLevels = value, () -> this.vanillaOpts.mipmapLevels)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                )
        );

        return qualityPage;
    }

    private OptionPageBuilder buildPerformancePage(ConfigBuilder builder) {
        var performancePage = builder.createOptionPage().setName(new TranslatableText("sodium.options.pages.performance"));

        performancePage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:performance.chunk_update_threads"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.chunk_update_threads.name"))
                                .setTooltip(new TranslatableText("sodium.options.chunk_update_threads.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.quantityOrDisabled(
                                        (v) -> new TranslatableText("sodium.options.chunk_update_threads.value", v),
                                        new TranslatableText("sodium.options.default")
                                ))
                                .setRange(0, Runtime.getRuntime().availableProcessors(), 1)
                                .setDefaultValue(DEFAULTS.performance.chunkBuilderThreads)
                                .setBinding(value -> this.sodiumOpts.performance.chunkBuilderThreads = value, () -> this.sodiumOpts.performance.chunkBuilderThreads)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:performance.use_async_culling"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.use_async_culling.name"))
                                .setTooltip(new TranslatableText("sodium.options.use_async_culling.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.useAsyncCulling)
                                .setBinding(value -> this.sodiumOpts.performance.useAsyncCulling = value, () -> this.sodiumOpts.performance.useAsyncCulling)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createEnumOption(new Identifier("radium:performance.always_defer_chunk_updates"), DeferMode.class)
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.defer_chunk_updates.name"))
                                .setTooltip(new TranslatableText("sodium.options.defer_chunk_updates.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.chunkBuildDeferMode)
                                .setBinding(value -> this.sodiumOpts.performance.chunkBuildDeferMode = value, () -> this.sodiumOpts.performance.chunkBuildDeferMode)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_UPDATE)
                )
        );

        performancePage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:performance.use_block_face_culling"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.use_block_face_culling.name"))
                                .setTooltip(new TranslatableText("sodium.options.use_block_face_culling.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.useBlockFaceCulling)
                                .setBinding(value -> this.sodiumOpts.performance.useBlockFaceCulling = value, () -> this.sodiumOpts.performance.useBlockFaceCulling)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:performance.use_fog_occlusion"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.use_fog_occlusion.name"))
                                .setTooltip(new TranslatableText("sodium.options.use_fog_occlusion.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.useFogOcclusion)
                                .setBinding(value -> this.sodiumOpts.performance.useFogOcclusion = value, () -> this.sodiumOpts.performance.useFogOcclusion)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_UPDATE)
                )
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:performance.vbos"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(new TranslatableText("options.vbo"))
                                .setTooltip(new TranslatableText("sodium.options.vbos.tooltip"))
                                .setDefaultValue(false)
                                .setBinding(value -> this.vanillaOpts.vbo = value, () -> this.vanillaOpts.vbo)
                                .setImpact(OptionImpact.MEDIUM)
                )
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:performance.use_entity_culling"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.use_entity_culling.name"))
                                .setTooltip(new TranslatableText("sodium.options.use_entity_culling.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.useEntityCulling)
                                .setBinding(value -> this.sodiumOpts.performance.useEntityCulling = value, () -> this.sodiumOpts.performance.useEntityCulling)
                                .setImpact(OptionImpact.MEDIUM)
                )

                .addOption(
                        builder.createBooleanOption(new Identifier("radium:performance.smart_cull"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.smart_cull.name"))
                                .setTooltip(new TranslatableText("sodium.options.smart_cull.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.smartCull)
                                .setBinding(value -> this.sodiumOpts.performance.smartCull = value, () -> this.sodiumOpts.performance.smartCull)
                                .setImpact(OptionImpact.MEDIUM)
                )
        );

        if (PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment()) {
            performancePage.addOptionGroup(builder.createOptionGroup()
                    .addOption(
                            builder.createEnumOption(new Identifier("radium:performance.quad_splitting"), QuadSplittingMode.class)
                                    .setStorageHandler(this.sodiumStorage)
                                    .setName(new TranslatableText("sodium.options.quad_splitting.name"))
                                    .setTooltip(new TranslatableText("sodium.options.quad_splitting.tooltip"))
                                    .setDefaultValue(DEFAULTS.performance.quadSplittingMode)
                                    .setBinding(value -> this.sodiumOpts.performance.quadSplittingMode = value, () -> this.sodiumOpts.performance.quadSplittingMode)
                                    .setImpact(OptionImpact.MEDIUM)
                                    .setEnabledProvider((state) -> SodiumClientMod.options().debug.terrainSortingEnabled)
                                    .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                    )
            );
        }

        return performancePage;
    }

    private OptionPageBuilder buildAdvancedPage(ConfigBuilder builder) {
        var advancedPage = builder.createOptionPage().setName(new TranslatableText("sodium.options.pages.advanced"));

        boolean isPersistentMappingSupported = MappedStagingBuffer.isSupported(RenderDevice.INSTANCE);

        advancedPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:advanced.use_persistent_mapping"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.use_persistent_mapping.name"))
                                .setTooltip(new TranslatableText("sodium.options.use_persistent_mapping.tooltip"))
                                .setDefaultValue(DEFAULTS.advanced.useAdvancedStagingBuffers)
                                .setBinding(value -> this.sodiumOpts.advanced.useAdvancedStagingBuffers = value, () -> this.sodiumOpts.advanced.useAdvancedStagingBuffers)
                                .setEnabled(isPersistentMappingSupported)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
        );

        advancedPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:advanced.cpu_render_ahead"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.cpu_render_ahead.name"))
                                .setTooltip(new TranslatableText("sodium.options.cpu_render_ahead.tooltip"))
                                .setDefaultValue(DEFAULTS.advanced.cpuRenderAhead)
                                .setBinding(value -> this.sodiumOpts.advanced.cpuRenderAhead = value, () -> this.sodiumOpts.advanced.cpuRenderAhead)
                )
                .addOption(
                        builder.createIntegerOption(new Identifier("radium:advanced.cpu_render_ahead_limit"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.cpu_render_ahead_limit.name"))
                                .setTooltip(new TranslatableText("sodium.options.cpu_render_ahead_limit.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.translateVariable("sodium.options.cpu_render_ahead_limit.value"))
                                .setRange(0, 9, 1)
                                .setDefaultValue(DEFAULTS.advanced.cpuRenderAheadLimit)
                                .setBinding(value -> this.sodiumOpts.advanced.cpuRenderAheadLimit = value, () -> this.sodiumOpts.advanced.cpuRenderAheadLimit)
                )
        );

        advancedPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(new Identifier("radium:advanced.fps_overlay"))
                                .setStorageHandler(this.sodiumStorage)
                                .setName(new TranslatableText("sodium.options.fps_overlay.name"))
                                .setTooltip(new TranslatableText("sodium.options.fps_overlay.tooltip"))
                                .setDefaultValue(DEFAULTS.advanced.fpsOverlay)
                                .setBinding(value -> this.sodiumOpts.advanced.fpsOverlay = value, () -> this.sodiumOpts.advanced.fpsOverlay)
                )
        );

        return advancedPage;
    }
}
