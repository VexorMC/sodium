package net.caffeinemc.mods.sodium.client.config.structure;

import net.caffeinemc.mods.sodium.client.config.search.SearchIndex;
import net.minecraft.text.Text;

import java.util.List;

public record OptionGroup(Text name, List<Option> options) {
    public void registerTextSources(SearchIndex index, ModOptions modOptions, OptionPage page) {
        for (Option option : this.options) {
            option.registerTextSources(index, modOptions, page, this);
        }
    }
}
