package net.caffeinemc.mods.sodium.api.config.structure;

import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public interface OptionBuilder {
    OptionBuilder setName(Text name);

    OptionBuilder setTooltip(Text tooltip);

    OptionBuilder setEnabled(boolean available);

    OptionBuilder setEnabledProvider(Function<ConfigState, Boolean> provider, Identifier... dependencies);
}
