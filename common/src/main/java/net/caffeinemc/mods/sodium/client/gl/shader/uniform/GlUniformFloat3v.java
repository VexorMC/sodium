package net.caffeinemc.mods.sodium.client.gl.shader.uniform;

import org.lwjgl.opengl.GL20;

public class GlUniformFloat3v extends GlUniform<float[]> {
    public GlUniformFloat3v(int index) {
        super(index);
    }

    @Override
    public void set(float[] value) {
        if (value.length != 3) {
            throw new IllegalArgumentException("value.length != 3");
        }

        GL20.glUniform3f(this.index, value[0], value[1], value[2]);
    }

    public void set(float x, float y, float z) {
        GL20.glUniform3f(this.index, x, y, z);
    }
}
