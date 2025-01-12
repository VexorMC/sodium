package dev.vexor.radium.extra.client.gui.options.storage;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import dev.vexor.radium.extra.client.gui.SodiumExtraGameOptions;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;

public class SodiumExtraOptionsStorage implements OptionStorage<SodiumExtraGameOptions> {
    private final SodiumExtraGameOptions options = SodiumExtraClientMod.options();

    @Override
    public SodiumExtraGameOptions getData() {
        return this.options;
    }

    @Override
    public void save() {
        this.options.writeChanges();
    }
}
