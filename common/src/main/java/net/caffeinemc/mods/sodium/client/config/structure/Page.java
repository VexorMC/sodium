package net.caffeinemc.mods.sodium.client.config.structure;

import com.google.common.collect.ImmutableList;
import net.caffeinemc.mods.sodium.client.config.search.SearchIndex;
import net.minecraft.text.Text;

public interface Page {
    Text name();

    ImmutableList<OptionGroup> groups();

    void registerTextSources(SearchIndex index, ModOptions modOptions);
}
