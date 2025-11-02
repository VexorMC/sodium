package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import java.nio.FloatBuffer;

import net.coderbot.iris.vendored.joml.Matrix3f;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.vertex.PoseStack;

import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderFogComponent;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX;
import static org.lwjgl.opengl.GL11.GL_PROJECTION_MATRIX;
import static org.lwjgl.opengl.GL11C.glGetFloatv;

public class IrisChunkProgram extends ChunkProgram {
	// Uniform variable binding indexes
	private final int uModelViewMatrix;
	private final int uNormalMatrix;

	@Nullable
	private final ProgramUniforms irisProgramUniforms;

	@Nullable
	private final ProgramSamplers irisProgramSamplers;

	@Nullable
	private final ProgramImages irisProgramImages;

    private final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projection = BufferUtils.createFloatBuffer(16);

    public IrisChunkProgram(RenderDevice owner, Identifier name, int handle,
                            @Nullable ProgramUniforms irisProgramUniforms, @Nullable ProgramSamplers irisProgramSamplers,
                            @Nullable ProgramImages irisProgramImages) {
		super(owner, name, handle, ChunkShaderFogComponent.None::new);
		this.uModelViewMatrix = this.getUniformLocation("iris_ModelViewMatrix");
		this.uNormalMatrix = this.getUniformLocation("iris_NormalMatrix");
		this.irisProgramUniforms = irisProgramUniforms;
		this.irisProgramSamplers = irisProgramSamplers;
		this.irisProgramImages = irisProgramImages;
	}

	public void setup(float modelScale, float textureScale) {
		super.setup(modelScale, textureScale);

		if (irisProgramUniforms != null) {
			irisProgramUniforms.update();
		}

		if (irisProgramSamplers != null) {
			irisProgramSamplers.update();
		}

		if (irisProgramImages != null) {
			irisProgramImages.update();
		}

        glGetFloatv(GL_MODELVIEW_MATRIX, modelView);
        glGetFloatv(GL_PROJECTION_MATRIX, projection);

        float[] mv = new Matrix3f(modelView).get(new float[16]);

        Matrix3f normalMatrix = new Matrix3f(
                mv[0], mv[1], mv[2],
                mv[4], mv[5], mv[6],
                mv[8], mv[9], mv[10]
        );
        normalMatrix.invert();
        normalMatrix.transpose();

        uniformMatrix(uModelViewMatrix, modelView);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer normalMatrixBuffer = stack.mallocFloat(9);
            normalMatrix.get(normalMatrixBuffer);
            uniformMatrix(uNormalMatrix, normalMatrixBuffer);
        }
	}

	@Override
	public int getUniformLocation(String name) {
		// NB: We pass through calls involving u_ModelViewProjectionMatrix, u_ModelScale, and u_TextureScale, since
		//     currently patched Iris shader programs use those.

		if ("iris_BlockTex".equals(name) || "iris_LightTex".equals(name)) {
			// Not relevant for Iris shader programs
			return -1;
		}

		try {
			return super.getUniformLocation(name);
		} catch (NullPointerException e) {
			// Suppress getUniformLocation
			return -1;
		}
	}

	private void uniformMatrix(int location, FloatBuffer matrix) {
		if (location == -1) {
			return;
		}
        IrisRenderSystem.uniformMatrix4fv(location, false, matrix);
	}
}
