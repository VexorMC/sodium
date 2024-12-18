package net.caffeinemc.mods.sodium.client.gl.shader;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;

/**
 * An enumeration over the supported OpenGL shader types.
 */
public enum ShaderType {
    VERTEX(GL20.GL_VERTEX_SHADER),
    GEOMETRY(GL32.GL_GEOMETRY_SHADER),
    TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER),
    TESS_EVALUATION(GL40.GL_TESS_EVALUATION_SHADER),
    FRAGMENT(GL20.GL_FRAGMENT_SHADER);

    public final int id;

    ShaderType(int id) {
        this.id = id;
    }

    public static ShaderType fromGlShaderType(int id) {
        for (ShaderType type : values()) {
            if (type.id == id) {
                return type;
            }
        }

        return null;
    }
}
