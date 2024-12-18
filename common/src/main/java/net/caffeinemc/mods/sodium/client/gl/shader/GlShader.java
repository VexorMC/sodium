package net.caffeinemc.mods.sodium.client.gl.shader;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.caffeinemc.mods.sodium.client.gl.GlObject;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * A compiled OpenGL shader object.
 */
public class GlShader extends GlObject {
    private static final Logger LOGGER = LogManager.getLogger(GlShader.class);

    private final Identifier name;

    public GlShader(ShaderType type, Identifier name, String src) {
        this.name = name;

        int handle = GL20.glCreateShader(type.id);
        GL20.glShaderSource(handle, src);
        GL20.glCompileShader(handle);

        String log = GL20.glGetShaderInfoLog(handle, 1000);

        if (!log.isEmpty()) {
            LOGGER.warn("Shader compilation log for " + this.name + ": " + log);
        }

        int result = GLX.gl20GetShaderi(handle, GL20.GL_COMPILE_STATUS);

        if (result != GL11.GL_TRUE) {
            throw new RuntimeException("Shader compilation failed, see log for details");
        }

        this.setHandle(handle);
    }

    public Identifier getName() {
        return this.name;
    }

    public void delete() {
        GL20.glDeleteShader(this.handle());

        this.invalidateHandle();
    }
}
