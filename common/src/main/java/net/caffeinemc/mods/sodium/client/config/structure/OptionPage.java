package net.caffeinemc.mods.sodium.client.config.structure;

import com.google.common.collect.ImmutableList;
import net.caffeinemc.mods.sodium.client.config.search.SearchIndex;
import net.minecraft.text.Text;

public record OptionPage(Text name, ImmutableList<OptionGroup> groups) implements Page {
    @Override
    public void registerTextSources(SearchIndex index, ModOptions modOptions) {
        for (OptionGroup group : this.groups()) {
            group.registerTextSources(index, modOptions, this);
        }
    }
}
