package net.caffeinemc.mods.sodium.api.config.structure;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public interface ExternalPageBuilder extends PageBuilder {
    ExternalPageBuilder setName(Text name);

    ExternalPageBuilder setScreenProvider(Consumer<Screen> currentScreenConsumer);
}
