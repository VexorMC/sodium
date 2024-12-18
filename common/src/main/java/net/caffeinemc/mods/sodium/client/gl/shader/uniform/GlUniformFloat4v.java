package net.caffeinemc.mods.sodium.client.gl.shader.uniform;

import org.lwjgl.opengl.GL20;

public class GlUniformFloat4v extends GlUniform<float[]> {
    public GlUniformFloat4v(int index) {
        super(index);
    }

    @Override
    public void set(float[] value) {
        if (value.length != 4) {
            throw new IllegalArgumentException("value.length != 4");
        }

        GL20.glUniform4f(this.index, value[0], value[1], value[2], value[3]);
    }

    public void set(float x, float y, float z, float w) {
        GL20.glUniform4f(this.index, x, y, z, w);
    }
}
