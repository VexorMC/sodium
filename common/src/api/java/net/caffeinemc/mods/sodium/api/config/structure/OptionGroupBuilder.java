package net.caffeinemc.mods.sodium.api.config.structure;

import net.minecraft.text.Text;

public interface OptionGroupBuilder {
    OptionGroupBuilder setName(Text name);

    OptionGroupBuilder addOption(OptionBuilder option);
}
