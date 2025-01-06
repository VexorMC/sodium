package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11.*;

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
        GlStateManager.activeTexture(GLX.textureUnit);
        MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);

        drawQuads();
    }

    public static void drawQuads() {
        float width = (float) Display.getWidth();
        float height = (float) Display.getHeight();
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }

    public static void end() {
		RenderSystem.enableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.popMatrix();
	}
}
