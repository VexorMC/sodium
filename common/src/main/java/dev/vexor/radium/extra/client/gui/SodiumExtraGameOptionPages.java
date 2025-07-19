package dev.vexor.radium.extra.client.gui;

import com.google.common.collect.ImmutableList;
import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import dev.vexor.radium.extra.client.gui.options.control.SliderControlExtended;
import dev.vexor.radium.extra.client.gui.options.storage.SodiumExtraOptionsStorage;
import dev.vexor.radium.extra.util.ControlValueFormatterExtended;
import net.caffeinemc.mods.sodium.client.gui.options.*;
import net.caffeinemc.mods.sodium.client.gui.options.control.CyclingControl;
import net.caffeinemc.mods.sodium.client.gui.options.control.TickBoxControl;
import net.caffeinemc.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.*;

public class SodiumExtraGameOptionPages {
    public static final SodiumExtraOptionsStorage sodiumExtraOpts = new SodiumExtraOptionsStorage();
    public static final MinecraftOptionsStorage vanillaOpts = new MinecraftOptionsStorage();


    public static OptionPage animation() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(new LiteralText("Animations"))
                        .setTooltip(new TranslatableText("sodium-extra.option.animations_all.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.animation = value, opts -> opts.animationSettings.animation)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(new LiteralText(Blocks.WATER.getTranslatedName()))
                        .setTooltip(new TranslatableText("sodium-extra.option.animate_water.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.water = value, opts -> opts.animationSettings.water)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(new TranslatableText(Blocks.LAVA.getTranslatedName()))
                        .setTooltip(new TranslatableText("sodium-extra.option.animate_lava.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.lava = value, opts -> opts.animationSettings.lava)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(new TranslatableText(Blocks.FIRE.getTranslatedName()))
                        .setTooltip(new TranslatableText("sodium-extra.option.animate_fire.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.fire = value, opts -> opts.animationSettings.fire)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(new TranslatableText(Blocks.NETHER_PORTAL.getTranslatedName()))
                        .setTooltip(new TranslatableText("sodium-extra.option.animate_portal.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.portal = value, opts -> opts.animationSettings.portal)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.block_animations"))
                        .setTooltip(new TranslatableText("sodium-extra.option.block_animations.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.animationSettings.blockAnimations = value, options -> options.animationSettings.blockAnimations)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .build());
        return new OptionPage(new TranslatableText("sodium-extra.option.animations"), ImmutableList.copyOf(groups));
    }

    public static OptionPage particle() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(new LiteralText("Particles"))
                        .setTooltip(new TranslatableText("sodium-extra.option.particles_all.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.particles = value, opts -> opts.particleSettings.particles)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(new LiteralText("Rain Splash"))
                        .setTooltip(new TranslatableText("sodium-extra.option.rain_splash.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.rainSplash = value, opts -> opts.particleSettings.rainSplash)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(new LiteralText("Block Break"))
                        .setTooltip(new TranslatableText("sodium-extra.option.block_break.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.blockBreak = value, opts -> opts.particleSettings.blockBreak)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(new LiteralText("Block Breaking"))
                        .setTooltip(new TranslatableText("sodium-extra.option.block_breaking.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.blockBreaking = value, opts -> opts.particleSettings.blockBreaking)
                        .build()
                )
                .build());

        return new OptionPage(new TranslatableText("options.particles"), ImmutableList.copyOf(groups));
    }

    public static OptionPage detail() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.sky"))
                        .setTooltip(new TranslatableText("sodium-extra.option.sky.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.sky = value, opts -> opts.detailSettings.sky)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.stars").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.stars"))
                        .setTooltip(new TranslatableText("sodium-extra.option.stars.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.stars = value, opts -> opts.detailSettings.stars)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sun_moon").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.sun"))
                        .setTooltip(new TranslatableText("sodium-extra.option.sun.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.sun = value, opts -> opts.detailSettings.sun)
                        .build()
                ).add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sun_moon").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.moon"))
                        .setTooltip(new TranslatableText("sodium-extra.option.moon.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.moon = value, opts -> opts.detailSettings.moon)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(new LiteralText("Rain & Snow"))
                        .setTooltip(new TranslatableText("sodium-extra.option.rain_snow.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.rainSnow = value, opts -> opts.detailSettings.rainSnow)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.biome_colors").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.biome_colors"))
                        .setTooltip(new TranslatableText("sodium-extra.option.biome_colors.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.detailSettings.biomeColors = value, options -> options.detailSettings.biomeColors)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky_colors").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.sky_colors"))
                        .setTooltip(new TranslatableText("sodium-extra.option.sky_colors.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.detailSettings.skyColors = value, options -> options.detailSettings.skyColors)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());
        return new OptionPage(new TranslatableText("sodium-extra.option.details"), ImmutableList.copyOf(groups));
    }

    public static OptionPage render() {
        List<OptionGroup> groups = new ArrayList<>();


        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(new LiteralText("Item Frame"))
                        .setTooltip(new TranslatableText("sodium-extra.option.item_frames.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.itemFrame = value, opts -> opts.renderSettings.itemFrame)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(new LiteralText("Armor Stand"))
                        .setTooltip(new TranslatableText("sodium-extra.option.armor_stands.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.armorStand = value, options -> options.renderSettings.armorStand)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(new LiteralText("Painting"))
                        .setTooltip(new TranslatableText("sodium-extra.option.paintings.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.painting = value, options -> options.renderSettings.painting)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.beacon_beam"))
                        .setTooltip(new TranslatableText("sodium-extra.option.beacon_beam.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.beaconBeam = value, opts -> opts.renderSettings.beaconBeam)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.enchanting_table_book"))
                        .setTooltip(new TranslatableText("sodium-extra.option.enchanting_table_book.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.enchantingTableBook = value, opts -> opts.renderSettings.enchantingTableBook)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(new LiteralText("Piston"))
                        .setTooltip(new TranslatableText("sodium-extra.option.piston.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.piston = value, options -> options.renderSettings.piston)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.item_frame_name_tag"))
                        .setTooltip(new TranslatableText("sodium-extra.option.item_frame_name_tag.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.itemFrameNameTag = value, opts -> opts.renderSettings.itemFrameNameTag)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.player_name_tag"))
                        .setTooltip(new TranslatableText("sodium-extra.option.player_name_tag.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.playerNameTag = value, options -> options.renderSettings.playerNameTag)
                        .build()
                )
                .build());
        return new OptionPage(new TranslatableText("sodium-extra.option.render"), ImmutableList.copyOf(groups));
    }

    public static OptionPage extra() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.reduce_resolution_on_mac").isEnabled() && MinecraftClient.IS_MAC)
                        .setName(new TranslatableText("sodium-extra.option.reduce_resolution_on_mac"))
                        .setTooltip(new TranslatableText("sodium-extra.option.reduce_resolution_on_mac.tooltip"))
                        .setEnabled(() -> MinecraftClient.IS_MAC)
                        .setImpact(OptionImpact.HIGH)
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.reduceResolutionOnMac = value, opts -> opts.extraSettings.reduceResolutionOnMac)
                        .build()
                ).build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName(new TranslatableText("sodium-extra.option.advanced_item_tooltips"))
                        .setTooltip(new TranslatableText("sodium-extra.option.advanced_item_tooltips.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advancedItemTooltips = value, opts -> opts.advancedItemTooltips)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.prevent_shaders").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.prevent_shaders"))
                        .setTooltip(new TranslatableText("sodium-extra.option.prevent_shaders.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.preventShaders = value, options -> options.extraSettings.preventShaders)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.steady_debug_hud"))
                        .setTooltip(new TranslatableText("sodium-extra.option.steady_debug_hud.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.steadyDebugHud = value, options -> options.extraSettings.steadyDebugHud)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                        .setName(new TranslatableText("sodium-extra.option.steady_debug_hud_refresh_interval"))
                        .setTooltip(new TranslatableText("sodium-extra.option.steady_debug_hud_refresh_interval.tooltip"))
                        .setControl(option -> new SliderControlExtended(option, 1, 20, 1, ControlValueFormatterExtended.ticks(), false))
                        .setBinding((options, value) -> options.extraSettings.steadyDebugHudRefreshInterval = value, options -> options.extraSettings.steadyDebugHudRefreshInterval)
                        .build()
                )
                .build());

        return new OptionPage(new TranslatableText("sodium-extra.option.extras"), ImmutableList.copyOf(groups));
    }
}
