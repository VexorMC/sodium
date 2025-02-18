package dev.vexor.radium.mixinconfig;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>A config option that can be enabled or disabled.</p>
 * 
 * <p>An option can be toggled by the user setting the name in the config file, 
 * or by a mod by declaring it in their {@code fabric.mod.json} file as a custom
 * value inside a custom value map with this mod's name.</p>
 *
 */
public final class Option {
    private final String name;

    private Object2BooleanLinkedOpenHashMap<Option> dependencies;
    private Set<String> modDefined = null;
    private boolean enabled;
    private boolean userDefined;
    private boolean overrideable;

    Option(String name, boolean enabled, boolean userDefined, boolean overrideable) {
        this.name = name;
        this.enabled = enabled;
        this.userDefined = userDefined;
        this.overrideable = overrideable;
    }

    /**
     * @return The name of this option
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return {@code true} if this option is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isEnabledRecursive(CaffeineConfig config) {
        return this.enabled && (config.getParent(this) == null || config.getParent(this).isEnabledRecursive(config));
    }

    /**
     * @return {@code true} if this option is overridden, {@code false} otherwise
     */
    public boolean isOverridden() {
        return this.isUserDefined() || this.isModDefined();
    }

    /**
     * @return {@code true} if this option's override is user defined, {@code false} otherwise
     */
    public boolean isUserDefined() {
        return this.userDefined;
    }

    /**
     * @return {@code true} if this option is overrideable, {@code false} otherwise
     */
    public boolean isOverrideable() {
        return this.overrideable;
    }

    /**
     * @return {@code true} if this option's override is mod defined, {@code false} otherwise
     */
    public boolean isModDefined() {
        return this.modDefined != null;
    }

    /**
     * @return An unmodifiable {@link Collection} of the mods that have defined overrides for this option
     */
    public Collection<String> getDefiningMods() {
        return this.modDefined != null ? Collections.unmodifiableCollection(this.modDefined) : Collections.emptyList();
    }

    void setOverrideable(boolean overrideable) {
        this.overrideable = overrideable;
    }

    void setEnabled(boolean enabled, boolean userDefined) {
        this.enabled = enabled;
        this.userDefined = userDefined;
    }

    void addModOverride(boolean enabled, String modId) {
        this.enabled = enabled;

        if (this.modDefined == null) {
            this.modDefined = new LinkedHashSet<>();
        }

        this.modDefined.add(modId);
    }

    void clearModsDefiningValue() {
        this.modDefined = null;
    }

    void addDependency(Option dependencyOption, boolean requiredValue) {
        if (this.dependencies == null) {
            this.dependencies = new Object2BooleanLinkedOpenHashMap<>(1);
        }
        this.dependencies.put(dependencyOption, requiredValue);
    }

    boolean disableIfDependenciesNotMet(Logger logger, CaffeineConfig config) {
        if (this.dependencies != null && this.isEnabled()) {
            for (Object2BooleanMap.Entry<Option> dependency : this.dependencies.object2BooleanEntrySet()) {
                Option option = dependency.getKey();
                boolean requiredValue = dependency.getBooleanValue();
                if (option.isEnabledRecursive(config) != requiredValue) {
                    this.enabled = false;
                    logger.warn("Option '{}' requires '{}={}' but found '{}'. Setting '{}={}'.", this.name, option.name, requiredValue, option.isEnabled(), this.name, this.enabled);
                    return true;
                }
            }
        }
        return false;
    }
}
