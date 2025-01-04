// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.irisshaders.iris.gl.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.KHRDebug;

public class ProgramCreator {
	private static final Logger LOGGER = LogManager.getLogger(ProgramCreator.class);

	public static int create(String name, GlShader... shaders) {
		int program = GL20.glCreateProgram();

		GL20.glBindAttribLocation(program, 11, "iris_Entity");
		GL20.glBindAttribLocation(program, 11, "mc_Entity");
		GL20.glBindAttribLocation(program, 12, "mc_midTexCoord");
		GL20.glBindAttribLocation(program, 13, "at_tangent");
		GL20.glBindAttribLocation(program, 14, "at_midBlock");

		GL20.glBindAttribLocation(program, 0, "Position");
		GL20.glBindAttribLocation(program, 1, "UV0");

		for (GlShader shader : shaders) {
			GLDebug.nameObject(KHRDebug.GL_SHADER, shader.getHandle(), shader.getName());

            GL20.glAttachShader(program, shader.getHandle());
		}

        GL20.glLinkProgram(program);

		GLDebug.nameObject(KHRDebug.GL_PROGRAM, program, name);

		//Always detach shaders according to https://www.khronos.org/opengl/wiki/Shader_Compilation#Cleanup
		for (GlShader shader : shaders) {
			IrisRenderSystem.detachShader(program, shader.getHandle());
		}

		String log = IrisRenderSystem.getProgramInfoLog(program);

		if (!log.isEmpty()) {
			LOGGER.warn("Program link log for " + name + ": " + log);
		}

		int result = GL20.glGetProgrami(program, GL20C.GL_LINK_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new ShaderCompileException(name, log);
		}

		return program;
	}
}
