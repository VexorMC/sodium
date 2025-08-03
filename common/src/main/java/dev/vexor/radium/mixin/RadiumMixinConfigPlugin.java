package dev.vexor.radium.mixin;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import dev.vexor.radium.mixin.config.AbstractCaffeineConfigMixinPlugin;
import dev.vexor.radium.mixin.config.CaffeineConfig;

public class RadiumMixinConfigPlugin extends AbstractCaffeineConfigMixinPlugin {
    @Override
    protected CaffeineConfig createConfig() {
        return SodiumExtraClientMod.mixinConfig();
    }

    @Override
    protected String mixinPackageRoot() {
        return "dev.vexor.radium.mixin.extra.";
    }
}
