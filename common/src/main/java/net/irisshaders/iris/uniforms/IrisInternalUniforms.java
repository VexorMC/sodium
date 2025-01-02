package net.irisshaders.iris.uniforms;

import dev.vexor.radium.compat.mojang.minecraft.render.FogHelper;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import org.joml.Vector4f;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

/**
 * Internal Iris uniforms that are not directly accessible by shaders.
 */
public class IrisInternalUniforms {
	private IrisInternalUniforms() {
		// no construction
	}

	public static void addFogUniforms(DynamicUniformHolder uniforms, FogMode fogMode) {
		uniforms
			.uniform4f(PER_FRAME, "iris_FogColor", () -> {
				float[] fogColor = FogHelper.getFogColor();
				return new Vector4f(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
			});

		uniforms.uniform1f(PER_FRAME, "iris_FogStart", FogHelper::getFogStart)
			.uniform1f(PER_FRAME, "iris_FogEnd", FogHelper::getFogEnd);

		uniforms.uniform1f("iris_FogDensity", () -> {
			// ensure that the minimum value is 0.0
			return Math.max(0.0F, CapturedRenderingState.INSTANCE.getFogDensity());
		}, notifier -> {
		});

		uniforms.uniform1f("iris_currentAlphaTest", CapturedRenderingState.INSTANCE::getCurrentAlphaTest, notifier -> {
		});

		// Optifine compatibility
		uniforms.uniform1f("alphaTestRef", CapturedRenderingState.INSTANCE::getCurrentAlphaTest, notifier -> {
		});
	}
}
