package net.irisshaders.iris.uniforms;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.minecraft.client.MinecraftClient;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

public class VanillaUniforms {
	public static void addVanillaUniforms(DynamicUniformHolder uniforms) {
		Vector2f cachedScreenSize = new Vector2f();
		// listener -> {} dictates we want this to run on every shader update, not just on a new frame. These are dynamic.
		uniforms.uniform1f("iris_LineWidth", () -> GL11.glGetFloat(GL11.GL_LINE_WIDTH), listener -> {
		});
		uniforms.uniform2f("iris_ScreenSize", () -> cachedScreenSize.set(MinecraftClient.getInstance().width, MinecraftClient.getInstance().height), listener -> {
		});
	}
}
