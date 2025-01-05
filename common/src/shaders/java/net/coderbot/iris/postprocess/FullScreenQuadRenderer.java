package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	public void render() {
		begin();

		renderQuad();

		end();
	}

	public void begin() {
		RenderSystem.disableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		// scale the quad from [0, 1] to [-1, 1]
		RenderSystem.translatef(-1.0F, -1.0F, 0.0F);
		RenderSystem.scalef(2.0F, 2.0F, 0.0F);

		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public void renderQuad() {
        var buffer = Tessellator.getInstance().getBuffer();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);

        buffer.vertex(1.0F, 1.0F, 0.0F).texture(1.0F, 1.0F).next();
        buffer.vertex(0.0F, 1.0F, 0.0F).texture(0.0F, 1.0F).next();
        buffer.vertex(1.0F, 0.0F, 0.0F).texture(1.0F, 0.0F).next();
        buffer.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).next();

        Tessellator.getInstance().draw();
    }

	public static void end() {
		RenderSystem.enableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.popMatrix();
	}
}
