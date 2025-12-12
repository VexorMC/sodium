package net.caffeinemc.mods.sodium.client.gui.options.control;

import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.function.IntFunction;

public class ControlValueFormatterImpls {
    private ControlValueFormatterImpls() {
    }

    public static ControlValueFormatter guiScale() {
        return (v) -> (v == 0) ? new TranslatableText("options.guiScale.auto") : new LiteralText(v + "x");
    }

    public static ControlValueFormatter resolution() {
        return (v) -> {
            return new LiteralText("");
        };
    }

    public static ControlValueFormatter fpsLimit() {
        return (v) -> (v == 260) ? new TranslatableText("options.framerateLimit.max") : new LiteralText(v + " FPS");
    }

    public static ControlValueFormatter brightness() {
        return (v) -> {
            if (v == 0) {
                return new TranslatableText("options.gamma.min");
            } else if (v == 100) {
                return new TranslatableText("options.gamma.max");
            } else {
                return new LiteralText(v + "%");
            }
        };
    }

    public static ControlValueFormatter biomeBlend() {
        return (v) -> {
            if (v < 0 || v > 7) {
                return new TranslatableText("parsing.int.invalid", v);
            } else if (v == 0) {
                return new TranslatableText("gui.none");
            } else {
                int sv = 2 * v + 1;
                return new TranslatableText("sodium.options.biome_blend.value", sv, sv);
            }
        };
    }

    public static ControlValueFormatter translateVariable(String key) {
        return (v) -> new TranslatableText(key, v);
    }

    public static ControlValueFormatter percentage() {
        return (v) -> new LiteralText(v + "%");
    }

    public static ControlValueFormatter multiplier() {
        return (v) -> new LiteralText(v + "x");
    }

    public static ControlValueFormatter quantityOrDisabled(IntFunction<Text> valueText, Text disableText) {
        return (v) -> v == 0 ? disableText : valueText.apply(v);
    }

    public static ControlValueFormatter number() {
        return (v) -> new LiteralText(String.valueOf(v));
    }
}
