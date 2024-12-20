package net.caffeinemc.mods.sodium.client.gui.options.named;

import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum GraphicsMode implements TextProvider {
    FANCY("options.graphics.fancy"),
    FAST("options.graphics.fast");

    private final String name;

    GraphicsMode(String name) {
        this.name = name;
    }

    @Override
    public Text getLocalizedName() {
        return new TranslatableText(name);
    }

    public boolean isFancy() {
        return this == FANCY;
    }

    public static GraphicsMode fromBoolean(boolean isFancy) {
        return isFancy ? FANCY : FAST;
    }

}