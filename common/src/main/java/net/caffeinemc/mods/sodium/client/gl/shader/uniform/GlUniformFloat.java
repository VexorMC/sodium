package net.caffeinemc.mods.sodium.client.gl.shader.uniform;

import org.lwjgl.opengl.GL20;

public class GlUniformFloat extends GlUniform<Float> {
    public GlUniformFloat(int index) {
        super(index);
    }

    @Override
    public void set(Float value) {
        this.setFloat(value);
    }

    public void setFloat(float value) {
        GL20.glUniform1f(this.index, value);
    }
}
