package net.caffeinemc.mods.sodium.api.config.option;

import net.minecraft.text.Text;

public interface ControlValueFormatter {
    Text format(int value);
}
