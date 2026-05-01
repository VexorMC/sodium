package net.caffeinemc.mods.sodium.client.config.builder;

import net.caffeinemc.mods.sodium.api.config.structure.OptionBuilder;
import net.caffeinemc.mods.sodium.client.config.structure.StaticOption;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;

abstract class StaticOptionBuilderImpl<O extends StaticOption> extends OptionBuilderImpl<O> {
    private Text tooltip;

    StaticOptionBuilderImpl(Identifier id) {
        super(id);
    }

    @Override
    void validateData() {
        Validate.notNull(this.getTooltip(), "Tooltip must be set");
        Validate.notBlank(this.getTooltip().asFormattedString(), "Tooltip must not be blank");
    }

    Text getTooltip() {
        return getFirstNotNull(this.tooltip, StaticOption::getTooltip);
    }

    @Override
    public OptionBuilder setTooltip(Text tooltip) {
        Validate.notNull(tooltip, "Argument must not be null");

        this.tooltip = tooltip;
        return this;
    }
}
