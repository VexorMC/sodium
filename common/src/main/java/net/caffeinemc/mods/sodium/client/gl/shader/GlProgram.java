package net.caffeinemc.mods.sodium.client.gl.shader;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.caffeinemc.mods.sodium.client.gl.GlObject;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniform;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * An OpenGL shader program.
 */
public class GlProgram<T> extends GlObject implements ShaderBindingContext {
    private static final Logger LOGGER = LogManager.getLogger(GlProgram.class);

    private final T shaderInterface;

    protected GlProgram(int program, Function<ShaderBindingContext, T> interfaceFactory) {
        this.setHandle(program);
        this.shaderInterface = interfaceFactory.apply(this);
    }

    public T getInterface() {
        return this.shaderInterface;
    }

    public static Builder builder(Identifier name) {
        return new Builder(name);
    }

    public void bind() {
        GL20.glUseProgram(this.handle());
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public void delete() {
        GL20.glDeleteProgram(this.handle());

        this.invalidateHandle();
    }

    @Override
    public <U extends GlUniform<?>> @NotNull U bindUniform(String name, IntFunction<U> factory) {
        int index = GL20.glGetUniformLocation(this.handle(), name);

        if (index < 0) {
            throw new NullPointerException("No uniform exists with name: " + name);
        }

        return factory.apply(index);
    }

    @Override
    public <U extends GlUniform<?>> U bindUniformOptional(String name, IntFunction<U> factory) {
        int index = GL20.glGetUniformLocation(this.handle(), name);

        if (index < 0) {
            return null;
        }

        return factory.apply(index);
    }

    @Override
    public @NotNull GlUniformBlock bindUniformBlock(String name, int bindingPoint) {
        int index = GL31.glGetUniformBlockIndex(this.handle(), name);

        if (index < 0) {
            throw new NullPointerException("No uniform block exists with name: " + name);
        }

        GL31.glUniformBlockBinding(this.handle(), index, bindingPoint);

        return new GlUniformBlock(bindingPoint);
    }

    @Override
    public GlUniformBlock bindUniformBlockOptional(String name, int bindingPoint) {
        int index = GL31.glGetUniformBlockIndex(this.handle(), name);

        if (index < 0) {
            return null;
        }

        GL31.glUniformBlockBinding(this.handle(), index, bindingPoint);

        return new GlUniformBlock(bindingPoint);
    }

    public static class Builder {
        private final Identifier name;
        private final int program;

        public Builder(Identifier name) {
            this.name = name;
            this.program = GL20.glCreateProgram();
        }

        public Builder attachShader(GlShader shader) {
            GL20.glAttachShader(this.program, shader.handle());

            return this;
        }

        /**
         * Links the attached shaders to this program and returns a user-defined container which wraps the shader
         * program. This container can, for example, provide methods for updating the specific uniforms of that shader
         * set.
         *
         * @param factory The factory which will create the shader program's interface
         * @param <U> The interface type for the shader program
         * @return An instantiated shader container as provided by the factory
         */
        public <U> GlProgram<U> link(Function<ShaderBindingContext, U> factory) {
            GL20.glLinkProgram(this.program);

            String log = GL20.glGetProgramInfoLog(this.program, 1000);

            if (!log.isEmpty()) {
                LOGGER.warn("Program link log for " + this.name + ": " + log);
            }

            int result = GLX.gl20GetProgrami(this.program, GL20.GL_LINK_STATUS);

            if (result != GL11.GL_TRUE) {
                throw new RuntimeException("Shader program linking failed, see log for details");
            }

            return new GlProgram<>(this.program, factory);
        }

        public Builder bindAttribute(String name, int index) {
            GL20.glBindAttribLocation(this.program, index, name);

            return this;
        }

        public Builder bindFragmentData(String name, int index) {
            GL30.glBindFragDataLocation(this.program, index, name);

            return this;
        }
    }
}
