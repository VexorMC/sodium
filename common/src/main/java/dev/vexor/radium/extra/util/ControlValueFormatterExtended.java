package dev.vexor.radium.extra.util;

import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.minecraft.text.TranslatableText;

public interface ControlValueFormatterExtended extends ControlValueFormatter {

    static ControlValueFormatter fogDistance() {
        return (v) -> {
            if (v == 0) {
                return new TranslatableText("options.gamma.default");
            } else if (v == 33) {
                return new TranslatableText("options.off");
            } else {
                return new TranslatableText("options.chunks", v);
            }
        };
    }

    static ControlValueFormatter ticks() {
        return (v) -> new TranslatableText("sodium-extra.units.ticks", v);
    }
}
