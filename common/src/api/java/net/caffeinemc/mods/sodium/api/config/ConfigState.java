package net.caffeinemc.mods.sodium.api.config;

import net.minecraft.util.Identifier;

public interface ConfigState {
    Identifier UPDATE_ON_REBUILD = new Identifier("__meta__:update_on_rebuild");
    
    boolean readBooleanOption(Identifier id);

    int readIntOption(Identifier id);

    <E extends Enum<E>> E readEnumOption(Identifier id, Class<E> enumClass);
}
