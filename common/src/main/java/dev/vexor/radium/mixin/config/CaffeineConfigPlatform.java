package dev.vexor.radium.mixin.config;

public interface CaffeineConfigPlatform {
    void applyModOverrides(CaffeineConfig config, String jsonKey);
}
