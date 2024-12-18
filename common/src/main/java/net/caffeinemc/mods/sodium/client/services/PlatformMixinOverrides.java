package net.caffeinemc.mods.sodium.client.services;

import java.util.List;
import java.util.Objects;

public interface PlatformMixinOverrides {
    PlatformMixinOverrides INSTANCE = Services.load(PlatformMixinOverrides.class);

    static PlatformMixinOverrides getInstance() {
        return INSTANCE;
    }

    List<MixinOverride> applyModOverrides();

    public final class MixinOverride {
        private final String modId;
        private final String option;
        private final boolean enabled;

        public MixinOverride(String modId, String option, boolean enabled) {
            this.modId = modId;
            this.option = option;
            this.enabled = enabled;
        }

        public String modId() {
            return modId;
        }

        public String option() {
            return option;
        }

        public boolean enabled() {
            return enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MixinOverride that = (MixinOverride) o;
            return enabled == that.enabled &&
                    modId.equals(that.modId) &&
                    option.equals(that.option);
        }

        @Override
        public int hashCode() {
            return Objects.hash(modId, option, enabled);
        }

        @Override
        public String toString() {
            return "MixinOverride[" +
                    "modId='" + modId + '\'' +
                    ", option='" + option + '\'' +
                    ", enabled=" + enabled +
                    ']';
        }
    }
}
