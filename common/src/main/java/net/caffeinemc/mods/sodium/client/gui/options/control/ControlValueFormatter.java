package net.caffeinemc.mods.sodium.client.gui.options.control;


import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public interface ControlValueFormatter {
    static ControlValueFormatter guiScale() {
        return (v) -> (v == 0) ? new TranslatableText("options.guiScale.auto") : new LiteralText(v + "x");
    }

    static ControlValueFormatter resolution() {
        return (v) -> {
            if (Util.getOperatingSystem() != Util.OperatingSystem.WINDOWS) {
                return new TranslatableText("options.fullscreen.unavailable");
            } else if (0 == v) {
                return new TranslatableText("options.fullscreen.current");
            }
            return null;
        };
    }
    static ControlValueFormatter fpsLimit() {
        return (v) -> (v == 260) ? new TranslatableText("options.framerateLimit.max") : new LiteralText(v + " FPS");
    }

    static ControlValueFormatter brightness() {
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

    static ControlValueFormatter biomeBlend() {
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

    Text format(int value);

    static ControlValueFormatter chunks() {
        return (v) -> {
            if (v < 4) {
                new TranslatableText("options.renderDistance.tiny").asFormattedString();
            } else if (v < 8) {
                new TranslatableText("options.renderDistance.short").asFormattedString();
            } else if (v < 16) {
                new TranslatableText("options.renderDistance.normal").asFormattedString();
            } else if (v <= 24) {
                new TranslatableText("options.renderDistance.far").asFormattedString();
            }
            return new LiteralText(v + " chunks");
        };
    }

    static ControlValueFormatter translateVariable(String key) {
        return (v) -> new TranslatableText(key, v);
    }

    static ControlValueFormatter percentage() {
        return (v) -> new LiteralText(v + "%");
    }

    static ControlValueFormatter multiplier() {
        return (v) -> new LiteralText(v + "x");
    }

    static ControlValueFormatter quantityOrDisabled(String name, String disableText) {
        return (v) -> new LiteralText(v == 0 ? disableText : v + " " + name);
    }

    static ControlValueFormatter number() {
        return (v) -> new LiteralText(String.valueOf(v));
    }
}
