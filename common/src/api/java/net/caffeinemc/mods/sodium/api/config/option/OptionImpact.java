package net.caffeinemc.mods.sodium.api.config.option;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

/**
 * Represents the performance impact level of a configuration option.
 */
public enum OptionImpact implements NameProvider {
    /**
     * Low impact on performance. Changing this option won't affect performance in a measurable or noticeable way.
     */
    LOW(Formatting.GREEN, "sodium.option_impact.low"),
    
    /**
     * Medium impact on performance. Changing this option may have a noticeable effect on performance in some scenarios and some systems.
     */
    MEDIUM(Formatting.YELLOW, "sodium.option_impact.medium"),
    
    /**
     * High impact on performance. Changing this option will likely have a significant effect on performance in most scenarios.
     */
    HIGH(Formatting.GOLD, "sodium.option_impact.high"),
    
    /**
     * Varies in impact on performance. The effect of changing this option on performance is highly dependent on the specific scenario and system.
     */
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
