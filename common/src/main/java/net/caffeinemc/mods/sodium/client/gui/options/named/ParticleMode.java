package net.caffeinemc.mods.sodium.client.gui.options.named;

import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum ParticleMode implements TextProvider {
    ALL("options.particles.all"),
    DECREASED("options.particles.decreased"),
    MINIMAL("options.particles.minimal");

    private static final ParticleMode[] VALUES = values();

    private final String name;

    ParticleMode(String name) {
        this.name = name;
    }

    @Override
    public Text getLocalizedName() {
        return new TranslatableText(name);
    }

    public static ParticleMode fromOrdinal(int ordinal) {
        return VALUES[ordinal];
    }
}