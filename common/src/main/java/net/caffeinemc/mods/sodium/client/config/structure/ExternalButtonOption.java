package net.caffeinemc.mods.sodium.client.config.structure;

import net.minecraft.client.gui.screen.Screen;
import net.caffeinemc.mods.sodium.client.config.value.DependentValue;
import net.caffeinemc.mods.sodium.client.gui.options.control.Control;
import net.caffeinemc.mods.sodium.client.gui.options.control.ExternalButtonControl;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.function.Consumer;

public class ExternalButtonOption extends StaticOption {
    final Consumer<Screen> currentScreenConsumer;

    public ExternalButtonOption(
            Identifier id,
            Collection<Identifier> dependencies,
            Text name,
            DependentValue<Boolean> enabled,
            Text tooltip,
            Consumer<Screen> currentScreenConsumer
    ) {
        super(id, dependencies, name, enabled, tooltip);
        this.currentScreenConsumer = currentScreenConsumer;
    }

    @Override
    Control createControl() {
        return new ExternalButtonControl(this, this.currentScreenConsumer);
    }

    public Consumer<Screen> getCurrentScreenConsumer() {
        return this.currentScreenConsumer;
    }
}
