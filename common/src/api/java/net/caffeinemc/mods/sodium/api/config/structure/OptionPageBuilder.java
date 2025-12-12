package net.caffeinemc.mods.sodium.api.config.structure;

import net.minecraft.text.Text;

public interface OptionPageBuilder extends PageBuilder {
    OptionPageBuilder setName(Text name);

    OptionPageBuilder addOptionGroup(OptionGroupBuilder group);
}
