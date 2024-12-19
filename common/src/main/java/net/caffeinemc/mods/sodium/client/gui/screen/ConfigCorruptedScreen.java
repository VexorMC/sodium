package net.caffeinemc.mods.sodium.client.gui.screen;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.console.Console;
import net.caffeinemc.mods.sodium.client.console.message.MessageLevel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigCorruptedScreen extends Screen {
    private static final String TEXT_BODY_RAW = """
        A problem occurred while trying to load the configuration file. This
        can happen when the file has been corrupted on disk, or when trying
        to manually edit the file by hand.
        
        If you continue, the configuration file will be reset back to known-good
        defaults, and you will lose any changes that have since been made to your
        Video Settings.
        
        More information about the error can be found in the log file.
        """;

    private static final List<Text> TEXT_BODY = Arrays.stream(TEXT_BODY_RAW.split("\n"))
            .map(LiteralText::new)
            .collect(Collectors.toList());

    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 20;

    private static final int SCREEN_PADDING = 32;

    private final @Nullable Screen prevScreen;
    private final Function<Screen, Screen> nextScreen;

    public ConfigCorruptedScreen(@Nullable Screen prevScreen, @Nullable Function<Screen, Screen> nextScreen) {

        this.prevScreen = prevScreen;
        this.nextScreen = nextScreen;
    }

    @Override
    public void init() {
        super.init();

        int buttonY = this.height - SCREEN_PADDING - BUTTON_HEIGHT;

        this.buttons.add(new ButtonWidget(
                69,
                this.width - SCREEN_PADDING - BUTTON_WIDTH, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                "Continue"
        ));
        this.buttons.add(new ButtonWidget(
                420,
                SCREEN_PADDING, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                "Go back"
        ));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        super.render(mouseX, mouseY, delta);

        this.textRenderer.draw("Sodium Renderer", 32, 32, 0xffffff);
        this.textRenderer.draw("Could not load the configuration file", 32, 48, 0xff0000);

        for (int i = 0; i < TEXT_BODY.size(); i++) {
            if (TEXT_BODY.get(i).asUnformattedString().isEmpty()) {
                continue;
            }

            this.textRenderer.draw(TEXT_BODY.get(i).asFormattedString(), 32, 68 + (i * 12), 0xffffff);
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        super.buttonClicked(button);

        switch (button.id) {
            case 69 -> MinecraftClient.getInstance().setScreen(this.prevScreen);
            case 420 -> {
                Console.instance().logMessage(MessageLevel.INFO, "sodium.console.config_file_was_reset", true, 3.0);

                SodiumClientMod.restoreDefaultOptions();
                assert this.nextScreen != null;
                MinecraftClient.getInstance().setScreen(this.nextScreen.apply(this.prevScreen));
            }
        }
    }
}
