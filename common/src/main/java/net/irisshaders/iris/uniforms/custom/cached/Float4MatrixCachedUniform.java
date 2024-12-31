package net.irisshaders.iris.uniforms.custom.cached;

import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.MatrixType;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.system.MemoryStack;

import java.util.function.Supplier;

public class Float4MatrixCachedUniform extends VectorCachedUniform<Matrix4fc> {
	final private float[] buffer = new float[16];

	public Float4MatrixCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Matrix4fc> supplier) {
		super(name, updateFrequency, new Matrix4f(), supplier);
	}

	@Override
	protected void setFrom(Matrix4fc other) {
		((Matrix4f) this.cached).set(other);
	}

	@Override
	public void push(int location) {
		// `gets` the values from the matrix and put's them into a buffer
		this.cached.get(buffer);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            GL20.glUniformMatrix4(location, false, stack.callocFloat(buffer.length).put(buffer).flip());
        }
	}

	@Override
	public MatrixType<Matrix4f> getType() {
		return MatrixType.MAT4;
	}
}
