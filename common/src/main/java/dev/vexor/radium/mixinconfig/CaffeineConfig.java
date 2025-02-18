package dev.vexor.radium.mixinconfig;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * <p>A mixin configuration object. Holds the {@link Option options} defined and handles overrides.</p>
 * 
 * @see CaffeineConfig.Builder
 */
@SuppressWarnings("CanBeFinal")
public final class CaffeineConfig {
    private final Map<String, Option> options = new HashMap<>();
    private final Set<Option> optionsWithDependencies = new ObjectLinkedOpenHashSet<>();
    private final String modName;
    private Logger logger;

    private static final CaffeineConfigPlatform PLATFORM = ServiceLoader.load(CaffeineConfigPlatform.class).findFirst().get();

    private CaffeineConfig(String modName) {
        this.modName = modName;
    }

    /**
     * <p>Creates and returns a {@link CaffeineConfig.Builder} that can be used to create a {@link CaffeineConfig} object.</p>
     * 
     * <p>Unless the methods in the builder are later called, the given {@code modName} will be used to get the logger and the JSON key.</p>
     * <p>The default logger is the one gotten from {@link org.apache.logging.log4j.LogManager#getLogger(String)} with the name {@code modName+" Config"}, and the default
     * JSON key is {@code lowercase(modName):options}. For example, if {@code modName} is {@code ExampleMod}, logger will be {@code ExampleModConfig}
     * and JSON key will be {@code examplemod:options} </p>
     * 
     * @param modName The name of the mod. Must not be {@code null}
     * 
     * @return A new {@link CaffeineConfig.Builder} instance
     */
    public static CaffeineConfig.Builder builder(String modName) {
        CaffeineConfig config = new CaffeineConfig(modName);
        config.logger = LogManager.getLogger(modName + " Config");
        String jsonKey = modName.toLowerCase() + ":options";
        return config.new Builder().withSettingsKey(jsonKey);
    }

    /**
     * @return The mod name used to create this {@link CaffeineConfig}
     */
    public String getModName() {
        return modName;
    }

    /**
     * @return The logger from this {@link CaffeineConfig}
     */
    public Logger getLogger() {
		return logger;
	}

    /**
     * @see Builder#addOptionDependency(String, String, boolean)
     */
    @SuppressWarnings("SameParameterValue")
    private void addOptionDependency(String optionName, String dependency, boolean requiredValue) {
        String mixinOptionName = getMixinOptionName(optionName);
        Option option = this.options.get(mixinOptionName);
        if (option == null) {
            throw new IllegalArgumentException(String.format("Option %s for dependency '%s depends on %s=%s' not found", optionName, optionName, dependency, requiredValue));
        }
        String dependencyOptionName = getMixinOptionName(dependency);
        Option dependencyOption = this.options.get(dependencyOptionName);
        if (dependencyOption == null) {
            throw new IllegalArgumentException(String.format("Option %s for dependency '%s depends on %s=%s' not found", dependency, optionName, dependency, requiredValue));
        }
        option.addDependency(dependencyOption, requiredValue);
        this.optionsWithDependencies.add(option);
    }

    /**
     * @see Builder#addMixinOption(String, boolean)
     */
    private void addMixinOption(String mixin, boolean enabled) {
        this.addMixinOption(mixin, enabled, true);
    }

    /**
     * @see Builder#addMixinOption(String, boolean, boolean)
     */
    public void addMixinOption(String mixin, boolean enabled, boolean overrideable) {
        String name = getMixinOptionName(mixin);

        if (this.options.putIfAbsent(name, new Option(name, enabled, false, overrideable)) != null) {
            throw new IllegalStateException("Mixin option already defined: " + mixin);
        }
    }

    private void readProperties(Properties props) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            Option option = this.options.get(key);

            if (option == null) {
                logger.warn("No configuration key exists with name '{}', ignoring", key);
                continue;
            }

            if (!option.isOverrideable()) {
                logger.warn("User attempted to override option '{}' that is not overrideable, ignoring", key);
                continue;
            }

            boolean enabled;

            if (value.equalsIgnoreCase("true")) {
                enabled = true;
            } else if (value.equalsIgnoreCase("false")) {
                enabled = false;
            } else {
                logger.warn("Invalid value '{}' encountered for configuration key '{}', ignoring", value, key);
                continue;
            }

            option.setEnabled(enabled, true);
        }
    }

    // Apply override-ability to children
    private void applyOverrideableChecks() {
        for (Option parentOption : this.options.values()) {
            for (Option childOption : this.options.values()) {
                if (childOption.getName().startsWith(parentOption.getName() + '.') && childOption != parentOption) {
                    if (!parentOption.isOverrideable() && childOption.isOverrideable()) {
                        logger.warn("Mixin option '{}' cannot be set as overrideable because its parent option '{}' is not overrideable. The mixin option will be treated as not overrideable.", childOption.getName(), parentOption.getName());
                        childOption.setOverrideable(false);
                    }
                }
            }
        }
    }

    private void applyChildOptionsStateChecks() {
        for (Option parentOption : this.options.values()) {
            for (Option childOption : this.options.values()) {
                if (childOption.getName().startsWith(parentOption.getName() + '.') && childOption != parentOption) {
                    if (childOption.isOverrideable() && (parentOption.isUserDefined() || parentOption.isModDefined())) {
                        childOption.setEnabled(parentOption.isEnabled(), parentOption.isUserDefined());
                        if (parentOption.isModDefined()) {
                            parentOption.getDefiningMods().forEach(mod -> childOption.addModOverride(parentOption.isEnabled(), mod));
                        }
                    } else {
                        if (parentOption.isUserDefined()) {
                            logger.warn("User attempted to override option '{}' that is not overrideable by overriding '{}', ignoring", childOption.getName(), parentOption.getName());
                        } else if (parentOption.isModDefined()) {
                            logger.warn("Mod '{}' attempted to override option '{}' that is not overrideable by overriding '{}', ignoring", parentOption.getDefiningMods(), childOption.getName(), parentOption.getName());
                        }
                    }
                }
            }
        }
    }

    void applyModOverride(String modId, String name, boolean enabled) {
        Option option = this.options.get(name);

        if (option == null) {
            logger.warn("Mod '{}' attempted to override option '{}', which doesn't exist, ignoring", modId, name);
            return;
        }

        if (!option.isOverrideable()) {
            logger.warn("Mod '{}' attempted to override option '{}' that is not overrideable, ignoring", modId, name);
            return;
        }

        // disabling the option takes precedence over enabling
        if (!enabled && option.isEnabled()) {
            option.clearModsDefiningValue();
        }

        if (!enabled || option.isEnabled() || option.getDefiningMods().isEmpty()) {
            option.addModOverride(enabled, modId);
        }
    }

    /**
     * Returns the effective option for the specified class name. This traverses the package path of the given mixin
     * and checks each root for configuration rules. If a configuration option disables a package, all mixins located in
     * that package and its children will be disabled. The effective option is that of the highest-priority option, either
     * a enable option at the end of the chain or a disable option at the earliest point in the chain.
     *
     * @return {@code null} if no options matched the given mixin name, otherwise the effective option for this Mixin
     */
    public Option getEffectiveOptionForMixin(String mixinClassName) {
        int lastSplit = 0;
        int nextSplit;

        Option option = null;

        while ((nextSplit = mixinClassName.indexOf('.', lastSplit)) != -1) {
            String key = getMixinOptionName(mixinClassName.substring(0, nextSplit));

            Option candidate = this.options.get(key);

            if (candidate != null) {
                option = candidate;

                if (!option.isEnabled()) {
                    return option;
                }
            }

            lastSplit = nextSplit + 1;
        }

        return option;
    }

    /**
     * Tests all dependencies and disables options when their dependencies are not met.
     */
    private boolean applyDependencies() {
        boolean changed = false;
        for (Option optionWithDependency : this.optionsWithDependencies) {
            changed |= optionWithDependency.disableIfDependenciesNotMet(logger, this);
        }
        return changed;
    }

    public Option getParent(Option option) {
        String optionName = option.getName();
        int split;

        if ((split = optionName.lastIndexOf('.')) != -1) {
            String key = optionName.substring(0, split);
            return this.options.get(key);

        }
        return null;
    }

    private static void writeDefaultConfig(Path file, String modName, String infoUrl) throws IOException {
        Path dir = file.getParent();

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        } else if (!Files.isDirectory(dir)) {
            throw new IOException("The parent file is not a directory");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(String.format("# This is the configuration file for %s.\n", modName));
            writer.write("# This file exists for debugging purposes and should not be configured otherwise.\n");
            writer.write("#\n");
            if (infoUrl != null) {
                writer.write("# You can find information on editing this file and all the available options here:\n");
                writer.write("# " + infoUrl + "\n");
                writer.write("#\n");
            }
            writer.write("# By default, this file will be empty except for this notice.\n");
        }
    }

    private static String getMixinOptionName(String name) {
        return "mixin." + name;
    }

    public int getOptionCount() {
        return this.options.size();
    }

    public int getOptionOverrideCount() {
        return (int) this.options.values()
                .stream()
                .filter(Option::isOverridden)
                .count();
    }

    public Map<String, Option> getOptions() {
        return ImmutableMap.copyOf(this.options);
    }

    /**
     * <p>A builder for {@link CaffeineConfig} instances.</p>
     * 
     * <p>Allows adding mixin options and creating depencencies between them, as well as 
     * configuring various properties from this config.</p>
     * 
     * @see CaffeineConfig#builder(String)
     */
    public final class Builder {
        private boolean alreadyBuilt = false;
        private String infoUrl;
        private String jsonKey;

        private Builder() {}

        /**
         * <p>Defines a Mixin option which can be configured by users and other mods.</p>
         *
         * @param mixin   The name of the mixin package which will be controlled by this option
         * @param enabled {@code true} if the option will be enabled by default, {@code false} otherwise
         * @throws IllegalStateException If an option with that name already exists
         */
        public Builder addMixinOption(String mixin, boolean enabled) {
            CaffeineConfig.this.addMixinOption(mixin, enabled);
            return this;
        }

        /**
         * <p>Defines a Mixin option which can be configured by users and other mods.</p>
         *
         * @param mixin   The name of the mixin package which will be controlled by this option
         * @param enabled {@code true} if the option will be enabled by default, {@code false} otherwise
         * @param overrideable {@code true} if an option is user/mod overrideable, {@code false} otherwise
         * @throws IllegalStateException If an option with that name already exists
         */
        public Builder addMixinOption(String mixin, boolean enabled, boolean overrideable) {
            CaffeineConfig.this.addMixinOption(mixin, enabled, overrideable);
            return this;
        }

        /**
         * <p>Defines a dependency between two registered mixin options. If a dependency is not satisfied, the mixin will
         * be disabled.</p>
         *
         * @param option        the mixin option that requires another option to be set to a given value
         * @param dependency    the mixin option the given option depends on
         * @param requiredValue the required value of the dependency
         * @throws IllegalArgumentException if one of the option don't exists
         */
        public Builder addOptionDependency(String option, String dependency, boolean requiredValue) {
            CaffeineConfig.this.addOptionDependency(option, dependency, requiredValue);
            return this;
        }

        /**
         * <p>Sets the logger the built {@link CaffeineConfig} will use, instead of one derived from the mod name</p>
         * @param logger The {@link Logger} to use. Can't be {@code null}
         */
        public Builder withLogger(Logger logger) {
            CaffeineConfig.this.logger = logger;
            return this;
        }

        /**
         * <p>Sets the key name to search in other mod's custom values in order to find overrides.</p>
         * @param key The key to search for
         */
        public Builder withSettingsKey(String key) {
            this.jsonKey = key;
            return this;
        }

        /**
         * <p>Sets the url to a resource with more information about the options to write in the config file header.</p>
         * 
         * <p>If it's {@code null} or not set, the paragraph about help on editing the file will be skipped</p>
         * @param url A {@link String} representing the url, or {@code null} to disable the paragraph
         */
        public Builder withInfoUrl(String url) {
            this.infoUrl = url;
            return this;
        }

        /**
         * <p>Builds a {@link CaffeineConfig} with the specified options, and populates the overrides for them.</p>
         * 
         * <p>This method will create a file in the given {@link Path} (and its parent directories if necessary) or 
         * read from it if it already exists.</p>
         * 
         * <p>It will also check for overrides in all loaded mods.</p>
         * 
         * <p>This method can only be called once per builder object</p>
         * 
         * @param path The {@link Path} to the settings file
         */
        public CaffeineConfig build(Path path) {
            if (alreadyBuilt) {
                throw new IllegalStateException("Cannot build a CaffeineConfig twice from the same builder");
            }

            applyOverrideableChecks();

            if (Files.exists(path)) {
                Properties props = new Properties();

                try (InputStream fin = Files.newInputStream(path)) {
                    props.load(fin);
                } catch (IOException e) {
                    throw new RuntimeException("Could not load config file", e);
                }

                readProperties(props);
            } else {
                try {
                    writeDefaultConfig(path, modName, infoUrl);
                } catch (IOException e) {
                    logger.warn("Could not write default configuration file", e);
                }
            }

            PLATFORM.applyModOverrides(CaffeineConfig.this, jsonKey);

            // Check dependencies several times, because one iteration may disable a option required by another option
            // This terminates because each additional iteration will disable one or more options, and there is only a finite number of rules
            while (applyDependencies());

            applyChildOptionsStateChecks();

            this.alreadyBuilt = true;

            return CaffeineConfig.this;
        }
    }
}
