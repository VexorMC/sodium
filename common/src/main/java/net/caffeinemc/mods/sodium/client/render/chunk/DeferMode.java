package net.caffeinemc.mods.sodium.client.render.chunk;

import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum DeferMode implements TextProvider {
    ALWAYS("sodium.options.defer_chunk_updates.always"),
    ONE_FRAME("sodium.options.defer_chunk_updates.one_frame"),
    ZERO_FRAMES("sodium.options.defer_chunk_updates.zero_frames");

    private final Text name;

    DeferMode(String name) {
        this.name = new TranslatableText(name);
    }

    @Override
    public Text getLocalizedName() {
        return this.name;
    }

    public boolean allowsUnlimitedUploadSize() {
        return this == ZERO_FRAMES;
    }
}