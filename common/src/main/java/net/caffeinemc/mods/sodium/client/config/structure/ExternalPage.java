package net.caffeinemc.mods.sodium.client.config.structure;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.screen.Screen;
import net.caffeinemc.mods.sodium.client.config.search.SearchIndex;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public record ExternalPage(Text name, Consumer<Screen> currentScreenConsumer) implements Page {
    @Override
    public ImmutableList<OptionGroup> groups() {
        return ImmutableList.of();
    }

    @Override
    public void registerTextSources(SearchIndex index, ModOptions modOptions) {
        // No-op
    }
}
