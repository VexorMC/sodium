package net.caffeinemc.mods.sodium.api.config.structure;

import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ExternalButtonOptionBuilder extends OptionBuilder {
    ExternalButtonOptionBuilder setScreenProvider(Consumer<Screen> currentScreenConsumer);

    @Override
    ExternalButtonOptionBuilder setName(Text name);

    @Override
    ExternalButtonOptionBuilder setTooltip(Text tooltip);

    @Override
    ExternalButtonOptionBuilder setEnabled(boolean available);

    @Override
    ExternalButtonOptionBuilder setEnabledProvider(Function<ConfigState, Boolean> provider, Identifier... dependencies);
}
