package net.caffeinemc.mods.sodium.client.gui.options.named;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;

public enum GraphicsMode implements TextProvider {
    FANCY("options.graphics.fancy"),
    FAST("options.graphics.fast");

    private final String name;

    GraphicsMode(String name) {
        this.name = name;
    }

    public static GraphicsMode fromBoolean(boolean isFancy) {
        return isFancy ? FANCY : FAST;
    }

    @Override
    public Text getLocalizedName() {
        return new TranslatableText(name);
    }

    public boolean isFancy() {
        return this == FANCY;
    }
}