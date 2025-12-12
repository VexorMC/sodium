package net.caffeinemc.mods.sodium.api.config.structure;

import net.minecraft.util.Identifier;

public interface OptionOverrideBuilder {
    OptionOverrideBuilder setTarget(Identifier target);

    OptionOverrideBuilder setReplacement(OptionBuilder option);
}
