package net.minecraft.client.renderer;


import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;

public record ShaderProgram(Identifier configId, VertexFormat vertexFormat, ShaderDefines defines) {
    @Override
    public String toString() {
        String string = String.valueOf(this.configId) + " (" + String.valueOf(this.vertexFormat) + ")";
        if (!this.defines.isEmpty()) {
            return string + " with " + String.valueOf(this.defines);
        }
        return string;
    }
}

