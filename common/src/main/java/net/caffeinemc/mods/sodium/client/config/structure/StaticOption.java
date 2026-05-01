package net.caffeinemc.mods.sodium.client.config.structure;

import net.caffeinemc.mods.sodium.client.config.value.DependentValue;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;

public abstract class StaticOption extends Option {
    final Text tooltip;

    StaticOption(
            Identifier id,
            Collection<Identifier> dependencies,
            Text name, DependentValue<Boolean> enabled,
            Text tooltip
    ) {
        super(id, dependencies, name, enabled);
        this.tooltip = tooltip;
    }

    @Override
    public Text getTooltip() {
        return this.tooltip;
    }
}
