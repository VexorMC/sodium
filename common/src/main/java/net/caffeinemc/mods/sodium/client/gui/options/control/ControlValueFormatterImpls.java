package net.caffeinemc.mods.sodium.client.gui.options.control;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;

import java.util.function.IntFunction;

public class ControlValueFormatterImpls {
    private ControlValueFormatterImpls() {
    }

    public static ControlValueFormatter guiScale() {
        return (v) -> (v == 0) ? new TranslatableText("options.guiScale.auto") : new LiteralText(v + "x");
    }


    public static ControlValueFormatter fpsLimit() {
        return (v) -> (v == 260) ? new TranslatableText("options.framerateLimit.max") : new TranslatableText("options.framerate", v);
    }

    public static ControlValueFormatter brightness() {
        return (v) -> {
            if (v == 0) {
                return new TranslatableText("options.gamma.min");
            } else if (v == 50) {
                return new TranslatableText("options.gamma.default");
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

    public static ControlValueFormatter anisotropyBit() {
        return (v -> {
            if (v == 0) {
                return new TranslatableText("options.off");
            } else {
                return new LiteralText((1 << v) + "x");
            }
        });
    }

    public static ControlValueFormatter chunkFade() {
        return (v -> {
            if (v == 0) {
                return new TranslatableText("gui.none");
            } else {
                return new TranslatableText("sodium.options.chunk_fade_time.value", (double) v / 1000.0);
            }
        });
    }
}
