package dev.vexor.radium.mixin.config;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.caffeinemc.mods.sodium.client.data.config.MixinConfig;
import dev.vexor.radium.mixin.MixinOption;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public abstract class AbstractCaffeineConfigMixinPlugin implements IMixinConfigPlugin {
    private CaffeineConfig config;
    private MixinConfig sodiumConfig;

    @Override
    public void onLoad(String mixinPackage) {
        this.config = createConfig();
        logger().info("Loaded configuration file for {}: {} options available, {} override(s) found",
                config.getModName(), this.config.getOptionCount(), this.config.getOptionOverrideCount());
        try {
            this.sodiumConfig = MixinConfig.load(new File("./config/radium-mixins.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Could not load configuration file for Radium", e);
        }
    }

    public static Optional<Path> getPaulscodePath() {
        for (Path path : FabricLauncherBase.getLauncher().getClassPath()) {
            if (path.toString().contains("librarylwjglopenal")) {
                return Optional.of(path);
            }
        }
        return Optional.empty();
    }

    /**
     * <p>Creates a {@link CaffeineConfig} to be checked against in this mixin plugin</p>
     * <p>This method will only be called once, on mixin plugin load</p>
     */
    protected abstract CaffeineConfig createConfig();

    /**
     * @return The root package where mixins are defined, ending with a dot
     */
    protected abstract String mixinPackageRoot();

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("dev.vexor.radium.mixin.sodium")) {
            String mixin = mixinClassName.substring("dev.vexor.radium.mixin.sodium.".length());
            MixinOption option = this.sodiumConfig.getEffectiveOptionForMixin(mixin);

            if (option == null) {
                logger().error("No rules matched mixin '{}', treating as foreign and disabling!", mixin);

                return false;
            }

            if (option.isOverridden()) {
                String source = "[unknown]";

                if (option.isUserDefined()) {
                    source = "user configuration";
                } else if (option.isModDefined()) {
                    source = "mods [" + String.join(", ", option.getDefiningMods()) + "]";
                }

                if (option.isEnabled()) {
                    this.logger().warn("Force-enabling mixin '{}' as rule '{}' (added by {}) enables it", mixin,
                            option.getName(), source);
                } else {
                    this.logger().warn("Force-disabling mixin '{}' as rule '{}' (added by {}) disables it and children", mixin,
                            option.getName(), source);
                }
            }

            return option.isEnabled();
        } else if (mixinClassName.startsWith("dev.vexor.radium.mixin.extra")) {
            String mixin = mixinClassName.substring(mixinPackageRoot().length());
            Option option = this.config.getEffectiveOptionForMixin(mixin);


            if (option == null) {
                throw new IllegalStateException(String.format("No options matched mixin '%s'! Mixins in this config must be under a registered option name", mixin));
            }

            if (option.isOverridden()) {
                String source = "[unknown]";

                if (option.isUserDefined()) {
                    source = "user configuration";
                } else if (option.isModDefined()) {
                    source = "mods [" + String.join(", ", option.getDefiningMods()) + "]";
                }

                if (option.isEnabled()) {
                    logger().warn("Force-enabling mixin '{}' as option '{}' (added by {}) enables it", mixin,
                            option.getName(), source);
                } else {
                    logger().warn("Force-disabling mixin '{}' as option '{}' (added by {}) disables it and children", mixin,
                            option.getName(), source);
                }
            }

            return option.isEnabled();
        }

        return true;
    }

    private Logger logger() {
        return config.getLogger();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

}
