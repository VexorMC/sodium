package net.caffeinemc.mods.sodium.client.gui.options.control;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.caffeinemc.mods.sodium.client.config.structure.ExternalButtonOption;
import net.caffeinemc.mods.sodium.client.config.structure.Option;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.util.Dim2i;


import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class ExternalButtonControl implements Control {
    public static final Text BASE_BUTTON_TEXT = new TranslatableText("sodium.options.open_external_page_button");

    private final ExternalButtonOption option;
    private final Consumer<Screen> currentScreenConsumer;

    public ExternalButtonControl(ExternalButtonOption option, Consumer<Screen> currentScreenConsumer) {
        this.option = option;
        this.currentScreenConsumer = currentScreenConsumer;
    }

    @Override
    public Option getOption() {
        return this.option;
    }

    @Override
    public ControlElement createElement(Screen screen, AbstractOptionList list, Dim2i dim, ColorTheme theme) {
        return new ExternalButtonControlElement(screen, list, dim, this.option, this.currentScreenConsumer, theme);
    }

    @Override
    public int getMaxWidth() {
        return Layout.BUTTON_LONG;
    }

    public static Text formatExternalButtonText(boolean enabled, ColorTheme theme) {
        if (enabled) {
            var enabledText = new LiteralText("");
            enabledText.append(BASE_BUTTON_TEXT.copy().setStyle(new Style().setFormatting(Formatting.UNDERLINE)));
            enabledText.append(new LiteralText(" >").copy().setStyle(new Style()));
            return enabledText;
        } else {
            return BASE_BUTTON_TEXT.copy().setStyle(new Style().setFormatting(Formatting.STRIKETHROUGH));
        }
    }

    private static class ExternalButtonControlElement extends ControlElement {
        private final Screen screen;
        private final ExternalButtonOption option;
        private final Consumer<Screen> currentScreenConsumer;

        public ExternalButtonControlElement(Screen screen, AbstractOptionList list, Dim2i dim, ExternalButtonOption option, Consumer<Screen> currentScreenConsumer, ColorTheme theme) {
            super(list, dim, theme);

            this.screen = screen;
            this.option = option;
            this.currentScreenConsumer = currentScreenConsumer;
        }

        @Override
        public Option getOption() {
            return this.option;
        }

        @Override
        public void render(int mouseX, int mouseY, float delta) {
            super.render(mouseX, mouseY, delta);

            Text buttonText = formatExternalButtonText(this.option.isEnabled(), this.theme);

            this.drawString(buttonText,
                    this.getLimitX() - Layout.OPTION_TEXT_SIDE_PADDING - this.font.getStringWidth(buttonText.asFormattedString()),
                    this.getCenterY() + Layout.REGULAR_TEXT_BASELINE_OFFSET,
                    Colors.FOREGROUND);

            if (this.isHovered()) {
            }
        }

        private void openScreen(Screen screen) {
            this.currentScreenConsumer.accept(screen);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.option.isEnabled() && button == 0 && this.isMouseOver(mouseX, mouseY)) {
                this.openScreen(this.screen);
                this.playClickSound();

                return true;
            }

            return false;
        }

        @Override
        public boolean keyPressed(int code, char character) {
            if (!isFocused()) return false;

//            if (event.isSelection()) {
//                this.openScreen(this.screen);
//                this.playClickSound();
//
//                return true;
//            }

            return false;
        }

    }
}
