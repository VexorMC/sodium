package net.coderbot.iris.mixin.gui;

import net.minecraft.client.Option;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VideoSettingsScreen.class)
public abstract class MixinVideoSettingsScreen extends Screen {
	protected MixinVideoSettingsScreen(Component title) {
		super(title);
	}

	@ModifyArg(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/Option;)V"
			),
			index = 0
	)
	private Option[] iris$addShaderPackScreenButton(Option[] old) {
		Option[] options = new Option[old.length + 2];
		System.arraycopy(old, 0, options, 0, old.length);
		options[options.length - 2] = new ShaderPackSelectionButtonOption((VideoSettingsScreen)(Object)this, this.minecraft);
		return options;
	}
}
