package net.caffeinemc.mods.sodium.client.config.builder;

import net.caffeinemc.mods.sodium.client.config.structure.Page;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;

public abstract class PageBuilderImpl {
    Text name;

    abstract Page build();

    void prepareBuild() {
        Validate.notNull(this.name, "Name must not be null");
    }

    public PageBuilderImpl setName(Text name) {
        this.name = name;
        return this;
    }
}
