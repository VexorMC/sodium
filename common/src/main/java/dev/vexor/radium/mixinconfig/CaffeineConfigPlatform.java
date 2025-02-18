package dev.vexor.radium.mixinconfig;

public interface CaffeineConfigPlatform {
    void applyModOverrides(CaffeineConfig config, String jsonKey);
}
