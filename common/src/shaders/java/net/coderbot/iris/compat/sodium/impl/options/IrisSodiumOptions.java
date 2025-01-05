package net.coderbot.iris.compat.sodium.impl.options;

import net.caffeinemc.mods.sodium.client.gui.options.OptionFlag;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpact;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.caffeinemc.mods.sodium.client.gui.options.control.CyclingControl;
import net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl;
import net.caffeinemc.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.LiteralText;

import java.io.IOException;

public class IrisSodiumOptions {
	public static OptionImpl<GameOptions, Integer> createMaxShadowDistanceSlider(MinecraftOptionsStorage vanillaOpts) {
		OptionImpl<GameOptions, Integer> maxShadowDistanceSlider = OptionImpl.createBuilder(int.class, vanillaOpts)
				.setName(new LiteralText("Max Shadow Distance"))
				.setTooltip(new LiteralText("The shadow render distance controls how far away terrain can potentially be rendered in the shadow pass. Lower distances mean that less terrain will be " +
                        "rendered, improving frame rates. This option cannot be changed on packs which explicitly specify a shadow render distance. The actual shadow render distance is capped by the " +
                        "View Distance setting."))
				.setControl(option -> new SliderControl(option, 0, 32, 1, ControlValueFormatter.chunks()))
				.setBinding((options, value) -> {
							IrisVideoSettings.shadowDistance = value;
							try {
								Iris.getIrisConfig().save();
							} catch (IOException e) {
								e.printStackTrace();
							}
						},
						options -> IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance))
				.setImpact(OptionImpact.HIGH)
				.setEnabled(IrisVideoSettings::isShadowDistanceSliderEnabled)
				.build();

		return maxShadowDistanceSlider;
	}

	public static OptionImpl<GameOptions, SupportedGraphicsMode> createLimitedVideoSettingsButton(MinecraftOptionsStorage vanillaOpts) {
		return OptionImpl.createBuilder(SupportedGraphicsMode.class, vanillaOpts)
				.setName(new LiteralText("Graphics Quality"))
				.setTooltip(new LiteralText("The default graphics quality controls some legacy options and is necessary for mod compatibility. If the options below are left to " +
						"\"Default\", they will use this setting. Fabulous graphics are blocked while shaders are enabled."))
				.setControl(option -> new CyclingControl<>(option, SupportedGraphicsMode.class, new LiteralText[] { new LiteralText("Fast"), new LiteralText("Fancy") }))
				.setBinding(
						(opts, value) -> opts.fancyGraphics = value.toVanilla(),
						opts -> SupportedGraphicsMode.fromVanilla(opts.fancyGraphics))
				.setImpact(OptionImpact.HIGH)
				.setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
				.build();
	}
}
