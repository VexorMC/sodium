package net.caffeinemc.mods.sodium.client.gui;

import com.google.common.collect.ImmutableList;
import net.caffeinemc.mods.sodium.client.gl.arena.staging.MappedStagingBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.gui.options.*;
import net.caffeinemc.mods.sodium.client.gui.options.control.*;
import net.caffeinemc.mods.sodium.client.gui.options.named.GraphicsMode;
import net.caffeinemc.mods.sodium.client.gui.options.named.ParticleMode;
import net.caffeinemc.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.caffeinemc.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.List;

// TODO: Rename in Sodium 0.6
public class SodiumGameOptionPages {
    private static final SodiumOptionsStorage sodiumOpts = new SodiumOptionsStorage();
    private static final MinecraftOptionsStorage vanillaOpts = new MinecraftOptionsStorage();
    private static final Window window = new Window(MinecraftClient.getInstance());

    public static OptionPage general() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TranslatableText("options.renderDistance"))
                        .setTooltip(new TranslatableText("sodium.options.view_distance.tooltip"))
                        .setControl(option -> new SliderControl(option, 2, 32, 1, ControlValueFormatter.chunks()))
                        .setBinding((options, value) -> options.viewDistance = (value), options -> options.viewDistance)
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TranslatableText("options.gamma"))
                        .setTooltip(new TranslatableText("sodium.options.brightness.tooltip"))
                        .setControl(opt -> new SliderControl(opt, 0, 100, 1, ControlValueFormatter.brightness()))
                        .setBinding((opts, value) -> opts.gamma = (value * 0.01f), (opts) -> (int) (opts.gamma / 0.01D))
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TranslatableText("options.guiScale"))
                        .setTooltip(new TranslatableText("sodium.options.gui_scale.tooltip"))
                        .setControl(option -> new SliderControl(option, 0, 3, 1, ControlValueFormatter.guiScale()))
                        .setBinding((opts, value) -> {
                            opts.guiScale = value;

                            // Resizing our window
                            if(MinecraftClient.getInstance().currentScreen instanceof SodiumOptionsGUI) {
                                MinecraftClient.getInstance().setScreen(new SodiumOptionsGUI(((SodiumOptionsGUI) MinecraftClient.getInstance().currentScreen).prevScreen));
                            }
                        }, opts -> opts.guiScale)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName(new TranslatableText("options.fullscreen"))
                        .setTooltip(new TranslatableText("sodium.options.fullscreen.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> {
                            opts.fullscreen = (value);

                            MinecraftClient client = MinecraftClient.getInstance();

                            if (client.isFullscreen() != opts.fullscreen) {
                                client.toggleFullscreen();

                                // The client might not be able to enter full-screen mode
                                opts.fullscreen = (client.isFullscreen());
                            }
                        }, (opts) -> opts.fullscreen)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName(new TranslatableText("options.vsync"))
                        .setTooltip(new TranslatableText("sodium.options.v_sync.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding(
                                (opts, value) -> {
                                    opts.vsync = value;
                                    Display.setVSyncEnabled(value);
                                },
                                opts -> opts.vsync)
                        .setImpact(OptionImpact.VARIES)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TranslatableText("options.framerateLimit"))
                        .setTooltip(new TranslatableText("sodium.options.fps_limit.tooltip"))
                        .setControl(option -> new SliderControl(option, 10, 260, 10, ControlValueFormatter.fpsLimit()))
                        .setBinding((opts, value) -> {
                            opts.maxFramerate = (value);
                        }, opts -> opts.maxFramerate)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName(new TranslatableText("options.viewBobbing"))
                        .setTooltip(new TranslatableText("sodium.options.view_bobbing.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding(
                                (opts, value) -> opts.bobView = value,
                                opts -> opts.bobView)
                        .build())
                .build());

        return new OptionPage(new TranslatableText("stat.generalButton"), ImmutableList.copyOf(groups));
    }

    public static OptionPage quality() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(GraphicsMode.class, vanillaOpts)
                        .setName(new TranslatableText("options.graphics"))
                        .setTooltip(new TranslatableText("sodium.options.graphics_quality.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, GraphicsMode.class))
                        .setBinding(
                                (opts, value) -> opts.fancyGraphics = value.isFancy(),
                                opts -> GraphicsMode.fromBoolean(opts.fancyGraphics))
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("options.renderClouds"))
                        .setTooltip(new TranslatableText("sodium.options.clouds_quality.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.quality.enableClouds = value, (opts) -> opts.quality.enableClouds)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .add(OptionImpl.createBuilder(int.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.cloud_height.name"))
                        .setTooltip(new TranslatableText("sodium.options.cloud_height.tooltip"))
                        .setControl(option -> new SliderControl(option, 128, 230, 1, ControlValueFormatter.number()))
                        .setBinding((opts, value) -> opts.quality.cloudHeight = value, opts -> opts.quality.cloudHeight)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(SodiumGameOptions.GraphicsQuality.class, sodiumOpts)
                        .setName(new TranslatableText("soundCategory.weather"))
                        .setTooltip(new TranslatableText("sodium.options.weather_quality.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.GraphicsQuality.class))
                        .setBinding((opts, value) -> opts.quality.weatherQuality = value, opts -> opts.quality.weatherQuality)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(SodiumGameOptions.GraphicsQuality.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.leaves_quality.name"))
                        .setTooltip(new TranslatableText("sodium.options.leaves_quality.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.GraphicsQuality.class))
                        .setBinding((opts, value) -> opts.quality.leavesQuality = value, opts -> opts.quality.leavesQuality)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(ParticleMode.class, vanillaOpts)
                        .setName(new TranslatableText("options.particles"))
                        .setTooltip(new TranslatableText("sodium.options.particle_quality.tooltip"))
                        .setControl(opt -> new CyclingControl<>(opt, ParticleMode.class))
                        .setBinding((opts, value) -> opts.particle = value.ordinal(), (opts) -> ParticleMode.fromOrdinal(opts.particle))
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(SodiumGameOptions.LightingQuality.class, sodiumOpts)
                        .setName(new TranslatableText("options.ao"))
                        .setTooltip(new TranslatableText("sodium.options.smooth_lighting.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.LightingQuality.class))
                        .setBinding((opts, value) -> opts.quality.smoothLighting = value, opts -> opts.quality.smoothLighting)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(int.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.biome_blend.name"))
                        .setTooltip(new TranslatableText("sodium.options.biome_blend.tooltip"))
                        .setControl(option -> new SliderControl(option, 1, 7, 1, ControlValueFormatter.biomeBlend()))
                        .setBinding((opts, value) -> opts.quality.biomeBlendRadius = (value), opts -> opts.quality.biomeBlendRadius)
                        .setImpact(OptionImpact.LOW)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                            .setName(new TranslatableText("options.entityShadows"))
                        .setTooltip(new TranslatableText("sodium.options.entity_shadows.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.entityShadows = (value), opts -> opts.entityShadows)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.vignette.name"))
                        .setTooltip(new TranslatableText("sodium.options.vignette.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.quality.enableVignette = value, opts -> opts.quality.enableVignette)
                        .build())
                .build());


        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TranslatableText("options.mipmapLevels"))
                        .setTooltip(new TranslatableText("sodium.options.mipmap_levels.tooltip"))
                        .setControl(option -> new SliderControl(option, 0, 4, 1, ControlValueFormatter.multiplier()))
                        .setBinding((opts, value) -> opts.mipmapLevels = (value), opts -> opts.mipmapLevels)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build())
                .build());


        return new OptionPage(new TranslatableText("sodium.options.pages.quality"), ImmutableList.copyOf(groups));
    }

    public static OptionPage performance() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.chunk_update_threads.name"))
                        .setTooltip(new TranslatableText("sodium.options.chunk_update_threads.tooltip"))
                        .setControl(o -> new SliderControl(o, 0, Runtime.getRuntime().availableProcessors(), 1, ControlValueFormatter.quantityOrDisabled("threads", "Default")))
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.performance.chunkBuilderThreads = value, opts -> opts.performance.chunkBuilderThreads)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.always_defer_chunk_updates.name"))
                        .setTooltip(new TranslatableText("sodium.options.always_defer_chunk_updates.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.performance.alwaysDeferChunkUpdates = value, opts -> opts.performance.alwaysDeferChunkUpdates)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_UPDATE)
                        .build())
                .build()
        );

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.use_block_face_culling.name"))
                        .setTooltip(new TranslatableText("sodium.options.use_block_face_culling.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.performance.useBlockFaceCulling = value, opts -> opts.performance.useBlockFaceCulling)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.use_fog_occlusion.name"))
                        .setTooltip(new TranslatableText("sodium.options.use_fog_occlusion.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.performance.useFogOcclusion = value, opts -> opts.performance.useFogOcclusion)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_UPDATE)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.use_entity_culling.name"))
                        .setTooltip(new TranslatableText("sodium.options.use_entity_culling.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.performance.useEntityCulling = value, opts -> opts.performance.useEntityCulling)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.smart_cull.name"))
                        .setTooltip(new TranslatableText("sodium.options.smart_cull.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.performance.smartCull = value, opts -> opts.performance.smartCull)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.animate_only_visible_textures.name"))
                        .setTooltip(new TranslatableText("sodium.options.animate_only_visible_textures.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.performance.animateOnlyVisibleTextures = value, opts -> opts.performance.animateOnlyVisibleTextures)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_UPDATE)
                        .build()
                )
                .build());

        if (PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment()) {
            groups.add(OptionGroup.createBuilder()
                    .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                            .setName(new TranslatableText("sodium.options.sort_behavior.name"))
                            .setTooltip(new TranslatableText("sodium.options.sort_behavior.tooltip"))
                            .setControl(TickBoxControl::new)
                            .setBinding((opts, value) -> opts.performance.sortingEnabled = value, opts -> opts.performance.sortingEnabled)
                            .setImpact(OptionImpact.LOW)
                            .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                            .build())
                    .build());
        }

        return new OptionPage(new TranslatableText("sodium.options.pages.performance"), ImmutableList.copyOf(groups));
    }

    public static OptionPage advanced() {
        List<OptionGroup> groups = new ArrayList<>();

        boolean isPersistentMappingSupported = MappedStagingBuffer.isSupported(RenderDevice.INSTANCE);

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.use_persistent_mapping.name"))
                        .setTooltip(new TranslatableText("sodium.options.use_persistent_mapping.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setEnabled(() -> isPersistentMappingSupported)
                        .setBinding((opts, value) -> opts.advanced.useAdvancedStagingBuffers = value, opts -> opts.advanced.useAdvancedStagingBuffers)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.cpu_render_ahead.name"))
                        .setTooltip(new TranslatableText("sodium.options.cpu_render_ahead.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.cpuRenderAhead = value, opts -> opts.advanced.cpuRenderAhead)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.cpu_render_ahead_limit.name"))
                        .setTooltip(new TranslatableText("sodium.options.cpu_render_ahead_limit.tooltip"))
                        .setControl(opt -> new SliderControl(opt, 0, 9, 1, ControlValueFormatter.translateVariable("sodium.options.cpu_render_ahead_limit.value")))
                        .setBinding((opts, value) -> opts.advanced.cpuRenderAheadLimit = value, opts -> opts.advanced.cpuRenderAheadLimit)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TranslatableText("sodium.options.fps_overlay.name"))
                        .setTooltip(new TranslatableText("sodium.options.fps_overlay.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.fpsOverlay = value, opts -> opts.advanced.fpsOverlay)
                        .build()
                )
                .build());

        return new OptionPage(new TranslatableText("sodium.options.pages.advanced"), ImmutableList.copyOf(groups));
    }

    public static OptionPage particleCulling() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new LiteralText("Particle Culling"))
                        .setTooltip(new LiteralText("Enable particle culling to improve performance by skipping rendering of particles that are not visible."))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.particleCulling.cullingEnabled = value, opts -> opts.particleCulling.cullingEnabled)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new LiteralText("Cull Behind Blocks"))
                        .setTooltip(new LiteralText("Enable culling of particles that are behind blocks. This can improve performance in some situations."))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleCulling.cullBehindBlocks = value, opts -> opts.particleCulling.cullBehindBlocks)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new LiteralText("Cull Behind Glass"))
                        .setTooltip(new LiteralText("Enable culling of particles that are behind glass blocks. This can improve performance in some situations."))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleCulling.cullBehindGlass = value, opts -> opts.particleCulling.cullBehindGlass)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new LiteralText("Cull In Spectator Mode"))
                        .setTooltip(new LiteralText("Enable culling of particles when in spectator mode. This can improve performance in spectator mode."))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleCulling.cullInSpectator = value, opts -> opts.particleCulling.cullInSpectator)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(int.class, sodiumOpts)
                        .setName(new LiteralText("Block Buffer"))
                        .setTooltip(new LiteralText("The minimum amount of blocks around the player that will be checked for culling. A higher value may improve performance but can also increase the chance of culling particles that are actually visible."))
                        .setControl(option -> new SliderControl(option, 0, 50, 1, ControlValueFormatter.number()))
                        .setBinding((opts, value) -> opts.particleCulling.blockBuffer = value, opts -> opts.particleCulling.blockBuffer)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .build());

        return new OptionPage(new LiteralText("Culling"), ImmutableList.copyOf(groups));
    }
}
