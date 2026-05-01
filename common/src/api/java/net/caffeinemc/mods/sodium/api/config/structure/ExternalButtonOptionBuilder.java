package net.caffeinemc.mods.sodium.api.config.structure;

import net.minecraft.client.gui.screen.Screen;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder interface for defining external button options.
 */
public interface ExternalButtonOptionBuilder extends OptionBuilder {
    @Override
    ExternalButtonOptionBuilder setName(Text name);

    @Override
    ExternalButtonOptionBuilder setTooltip(Text tooltip);

    @Override
    ExternalButtonOptionBuilder setEnabled(boolean available);

    @Override
    ExternalButtonOptionBuilder setEnabledProvider(Function<ConfigState, Boolean> provider, Identifier... dependencies);

    /** Sets the screen consumer for the external button option.
     *
     * @param currentScreenConsumer A consumer that accepts the current screen and opens the external configuration screen.
     * @return The current builder instance.
     */
    ExternalButtonOptionBuilder setScreenConsumer(Consumer<Screen> currentScreenConsumer);
}
