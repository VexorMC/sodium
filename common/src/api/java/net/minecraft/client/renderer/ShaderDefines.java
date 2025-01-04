package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ShaderDefines(Map<String, String> values, Set<String> flags) {
    public static final ShaderDefines EMPTY = new ShaderDefines(Map.of(), Set.of());

    public static Builder builder() {
        return new Builder();
    }

    public ShaderDefines withOverrides(ShaderDefines shaderDefines) {
        if (this.isEmpty()) {
            return shaderDefines;
        }
        if (shaderDefines.isEmpty()) {
            return this;
        }
        ImmutableMap.Builder builder = ImmutableMap.builder();
        builder.putAll(this.values);
        builder.putAll(shaderDefines.values);
        ImmutableSet.Builder builder2 = ImmutableSet.builder();
        builder2.addAll(this.flags);
        builder2.addAll(shaderDefines.flags);
        return new ShaderDefines((Map<String, String>)builder.build(), (Set<String>)builder2.build());
    }

    public String asSourceDirectives() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> object : this.values.entrySet()) {
            String string = object.getKey();
            String string2 = object.getValue();
            stringBuilder.append("#define ").append(string).append(" ").append(string2).append('\n');
        }
        for (String string : this.flags) {
            stringBuilder.append("#define ").append(string).append('\n');
        }
        return stringBuilder.toString();
    }

    public boolean isEmpty() {
        return this.values.isEmpty() && this.flags.isEmpty();
    }

    public static class Builder {
        private final ImmutableMap.Builder<String, String> values = ImmutableMap.builder();
        private final ImmutableSet.Builder<String> flags = ImmutableSet.builder();

        Builder() {
        }

        public Builder define(String string, String string2) {
            if (string2.isBlank()) {
                throw new IllegalArgumentException("Cannot define empty string");
            }
            this.values.put(string, Builder.escapeNewLines(string2));
            return this;
        }

        private static String escapeNewLines(String string) {
            return string.replaceAll("\n", "\\\\\n");
        }

        public Builder define(String string, float f) {
            this.values.put(string, String.valueOf(f));
            return this;
        }

        public Builder define(String string) {
            this.flags.add(string);
            return this;
        }

        public ShaderDefines build() {
            return new ShaderDefines((Map<String, String>)this.values.build(), (Set<String>)this.flags.build());
        }
    }
}

