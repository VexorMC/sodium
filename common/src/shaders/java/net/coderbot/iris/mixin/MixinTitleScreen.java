package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.SodiumVersionCheck;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Formatting;
import net.minecraft.Util;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.URISyntaxException;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
	private static boolean iris$hasFirstInit;

	protected MixinTitleScreen(Component arg) {
		super(arg);
	}

	@Inject(method = "init", at = @At("RETURN"))
	public void iris$showSodiumIncompatScreen(CallbackInfo ci) {
		if (iris$hasFirstInit) {
			return;
		}

		iris$hasFirstInit = true;

		String reason;

		if (!Iris.isSodiumInstalled() && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
			reason = "iris.sodium.failure.reason.notFound";
		} else if (Iris.isSodiumInvalid()) {
			reason = "iris.sodium.failure.reason.incompatible";
		} else if (Iris.hasNotEnoughCrashes()) {
			Minecraft.getInstance().setScreen(new ConfirmScreen(
				bool -> {
					if (bool) {
						Minecraft.getInstance().setScreen(this);
					} else {
						Minecraft.getInstance().stop();
					}
				},
				new TranslatableText("iris.nec.failure.title", Iris.MODNAME).withStyle(Formatting.BOLD, Formatting.RED),
				new TranslatableText("iris.nec.failure.description"),
				new TranslatableText("options.graphics.warning.accept").withStyle(Formatting.RED),
				new TranslatableText("menu.quit").withStyle(Formatting.BOLD)));
			return;
		} else {
			Iris.onLoadingComplete();

			return;
		}

		Minecraft.getInstance().setScreen(new ConfirmScreen(
				(boolean accepted) -> {
					if (accepted) {
						try {
							Util.getPlatform().openUri(new URI(SodiumVersionCheck.getDownloadLink()));
						} catch (URISyntaxException e) {
							throw new IllegalStateException(e);
						}
					} else {
						Minecraft.getInstance().stop();
					}
				},
				new TranslatableText("iris.sodium.failure.title").withStyle(Formatting.RED),
				new TranslatableText(reason),
				new TranslatableText("iris.sodium.failure.download"),
				new TranslatableText("menu.quit")));
	}
}
