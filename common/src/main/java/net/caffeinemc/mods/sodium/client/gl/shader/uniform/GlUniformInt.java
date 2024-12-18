package net.caffeinemc.mods.sodium.client.gl.shader.uniform;

import org.lwjgl.opengl.GL20;

public class GlUniformInt extends GlUniform<Integer> {
    public GlUniformInt(int index) {
        super(index);
    }

    @Override
    public void set(Integer value) {
        this.setInt(value);
    }

    public void setInt(int value) {
        GL20.glUniform1i(this.index, value);
    }
}
