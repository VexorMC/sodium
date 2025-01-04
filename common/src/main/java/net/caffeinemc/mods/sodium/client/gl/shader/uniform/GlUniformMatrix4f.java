package net.caffeinemc.mods.sodium.client.gl.shader.uniform;

import org.lwjgl.system.MemoryStack;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class GlUniformMatrix4f extends GlUniform<Matrix4fc>  {
    public GlUniformMatrix4f(int index) {
        super(index);
    }

    @Override
    public void set(Matrix4fc value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.callocFloat(16);
            value.get(buf);

            GL20.glUniformMatrix4fv(this.index, false, buf);
        }
    }
}
