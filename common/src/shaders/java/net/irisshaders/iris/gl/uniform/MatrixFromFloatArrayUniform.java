package net.irisshaders.iris.gl.uniform;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.Supplier;

public class MatrixFromFloatArrayUniform extends Uniform {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private final Supplier<float[]> value;
	private float[] cachedValue;

	MatrixFromFloatArrayUniform(int location, Supplier<float[]> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public void update() {
		float[] newValue = value.get();

		if (!Arrays.equals(newValue, cachedValue)) {
			cachedValue = Arrays.copyOf(newValue, 16);

			buffer.put(cachedValue);
			buffer.rewind();

            GL20.glUniformMatrix4fv(location, false, buffer);
		}
	}
}
