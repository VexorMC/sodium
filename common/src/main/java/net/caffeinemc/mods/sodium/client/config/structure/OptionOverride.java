package net.caffeinemc.mods.sodium.client.config.structure;

import net.minecraft.util.Identifier;

public record OptionOverride(Identifier target, String source, Option change) {
}
