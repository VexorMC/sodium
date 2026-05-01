package net.caffeinemc.mods.sodium.api.config.option;

import net.minecraft.text.Text;

/**
 * Base interface extended by enums whose members can provide display names.
 */
public interface NameProvider {
    /**
     * Gets the display name of this item.
     *
     * @return the display name
     */
    Text getName();
}
