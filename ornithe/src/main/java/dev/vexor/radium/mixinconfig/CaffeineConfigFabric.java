package dev.vexor.radium.mixinconfig;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.Map;

public class CaffeineConfigFabric implements CaffeineConfigPlatform {
    @Override
    public void applyModOverrides(CaffeineConfig config, String jsonKey) {
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = container.getMetadata();

            if (meta.containsCustomValue(jsonKey)) {
                CustomValue overrides = meta.getCustomValue(jsonKey);

                if (overrides.getType() != CustomValue.CvType.OBJECT) {
                    config.getLogger().warn("Mod '{}' contains invalid {} option overrides, ignoring", meta.getId(), config.getModName());
                    continue;
                }

                for (Map.Entry<String, CustomValue> entry : overrides.getAsObject()) {
                    if (entry.getValue().getType() != CustomValue.CvType.BOOLEAN) {
                        config.getLogger().warn("Mod '{}' attempted to override option '{}' with an invalid value, ignoring", meta.getId(), entry.getKey());
                        continue;
                    }

                    config.applyModOverride(meta.getId(), entry.getKey(), entry.getValue().getAsBoolean());
                }
            }
        }
    }
}
