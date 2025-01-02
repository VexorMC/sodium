package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.helpers.VertexBufferHelper;
import net.minecraft.client.render.*;
import org.lwjgl.opengl.GL11;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	private FullScreenQuadRenderer() {

	}

	public void render() {
		begin();

		renderQuad();

		end();
	}

	public void begin() {
		GlStateManager.disableDepthTest();

	}

	public void renderQuad() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        IrisRenderSystem.overridePolygonMode();
        bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).next();
        bufferBuilder.vertex(1.0F, 0.0F, 0.0F).texture(1.0F, 0.0F).next();
        bufferBuilder.vertex(1.0F, 1.0F, 0.0F).texture(1.0F, 1.0F).next();
        bufferBuilder.vertex(0.0F, 1.0F, 0.0F).texture(0.0F, 1.0F).next();
        Tessellator.getInstance().draw();
		IrisRenderSystem.restorePolygonMode();
	}

	public void end() {
		// NB: No need to clear the buffer state by calling glDisableVertexAttribArray - this VAO will always
		// have the same format, and buffer state is only associated with a given VAO, so we can keep it bound.
		//
		// Using quad.getFormat().clearBufferState() causes some Intel drivers to freak out:
		// https://github.com/IrisShaders/Iris/issues/1214

		GlStateManager.enableDepthTest();
	}
}
