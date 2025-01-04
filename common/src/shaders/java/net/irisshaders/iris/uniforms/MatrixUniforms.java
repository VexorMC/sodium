package net.irisshaders.iris.uniforms;

import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shadows.ShadowMatrices;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.function.Supplier;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public final class MatrixUniforms {
	private MatrixUniforms() {
	}

	public static void addMatrixUniforms(UniformHolder uniforms, PackDirectives directives) {
		addMatrix(uniforms, "ModelView", CapturedRenderingState.INSTANCE::getGbufferModelView);
		addMatrix(uniforms, "Projection", CapturedRenderingState.INSTANCE::getGbufferProjection);
		addDHMatrix(uniforms, "Projection", DHCompat::getProjection);
		addShadowMatrix(uniforms, "ModelView", () ->
			new Matrix4f(ShadowRenderer.createShadowModelView(directives.getSunPathRotation(), directives.getShadowDirectives().getIntervalSize(),
				Mth.equal(directives.getShadowDirectives().getNearPlane(), -1.0f) ? -DHCompat.getRenderDistance() * 16 : directives.getShadowDirectives().getNearPlane(),
				Mth.equal(directives.getShadowDirectives().getFarPlane(), -1.0f) ? DHCompat.getRenderDistance() * 16 : directives.getShadowDirectives().getFarPlane()
			).last().pose()));
		addShadowMatrix(uniforms, "Projection", () -> ShadowMatrices.createOrthoMatrix(directives.getShadowDirectives().getDistance(),
			Mth.equal(directives.getShadowDirectives().getNearPlane(), -1.0f) ? -DHCompat.getRenderDistance() * 16 : directives.getShadowDirectives().getNearPlane(),
			Mth.equal(directives.getShadowDirectives().getFarPlane(), -1.0f) ? DHCompat.getRenderDistance() * 16 : directives.getShadowDirectives().getFarPlane()));
	}

	private static void addMatrix(UniformHolder uniforms, String name, Supplier<Matrix4fc> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "gbuffer" + name, supplier)
			.uniformMatrix(PER_FRAME, "gbuffer" + name + "Inverse", new Inverted(supplier))
			.uniformMatrix(PER_FRAME, "gbufferPrevious" + name, new Previous(supplier));
	}

	private static void addDHMatrix(UniformHolder uniforms, String name, Supplier<Matrix4fc> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "dh" + name, supplier)
			.uniformMatrix(PER_FRAME, "dh" + name + "Inverse", new Inverted(supplier))
			.uniformMatrix(PER_FRAME, "dhPrevious" + name, new Previous(supplier));
	}

	private static void addShadowMatrix(UniformHolder uniforms, String name, Supplier<Matrix4fc> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "shadow" + name, supplier)
			.uniformMatrix(PER_FRAME, "shadow" + name + "Inverse", new Inverted(supplier));
	}

	private record Inverted(Supplier<Matrix4fc> parent) implements Supplier<Matrix4fc> {
		@Override
		public Matrix4fc get() {
			// PERF: Don't copy + allocate this matrix every time?
			Matrix4f copy = new Matrix4f(parent.get());

			copy.invert();

			return copy;
		}
	}

	private static class Previous implements Supplier<Matrix4fc> {
		private final Supplier<Matrix4fc> parent;
		private Matrix4f previous;

		Previous(Supplier<Matrix4fc> parent) {
			this.parent = parent;
			this.previous = new Matrix4f();
		}

		@Override
		public Matrix4fc get() {
			// PERF: Don't copy + allocate these matrices every time?
			Matrix4f copy = new Matrix4f(parent.get());
			Matrix4f previous = new Matrix4f(this.previous);

			this.previous = copy;

			return previous;
		}
	}
}
