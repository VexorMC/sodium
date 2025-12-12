package net.caffeinemc.mods.sodium.api.config.option;

import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;

public enum OptionImpact implements NameProvider {
    LOW(Formatting.GREEN, "sodium.option_impact.low"),
    MEDIUM(Formatting.YELLOW, "sodium.option_impact.medium"),
    HIGH(Formatting.GOLD, "sodium.option_impact.high"),
    VARIES(Formatting.WHITE, "sodium.option_impact.varies");

    private final Text text;

    OptionImpact(Formatting formatting, String text) {
        this.text = new TranslatableText(text)
                .setStyle(new Style().setFormatting(formatting));
    }

    @Override
    public Text getName() {
        return this.text;
    }
}
