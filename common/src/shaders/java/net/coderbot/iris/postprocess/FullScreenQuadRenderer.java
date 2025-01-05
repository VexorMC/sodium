package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20C;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	private final int quadBuffer;

	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	private FullScreenQuadRenderer() {
		this.quadBuffer = createQuad();
	}

	public void render() {
		begin();

		renderQuad();

		end();
	}

	@SuppressWarnings("deprecation")
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

        GL15.glBindBuffer(GL20C.GL_ARRAY_BUFFER, quadBuffer);
        VertexFormat vertexFormat = VertexFormats.POSITION_TEXTURE;
        int i = vertexFormat.getVertexSize();
        List<VertexFormatElement> list = vertexFormat.getElements();

        for(int j = 0; j < list.size(); ++j) {
            VertexFormatElement vertexFormatElement = (VertexFormatElement)list.get(j);
            VertexFormatElement.Type type = vertexFormatElement.getType();
            int k = vertexFormatElement.getFormat().getGlId();
            int l = vertexFormatElement.getIndex();
            switch (type) {
                case POSITION:
                    GL11.glVertexPointer(vertexFormatElement.getCount(), k, i, vertexFormat.getIndex(j));
                    GL11.glEnableClientState(32884);
                    break;
                case UV:
                    GLX.gl13ClientActiveTexture(GLX.textureUnit + l);
                    GL11.glTexCoordPointer(vertexFormatElement.getCount(), k, i, vertexFormat.getIndex(j));
                    GL11.glEnableClientState(32888);
                    GLX.gl13ClientActiveTexture(GLX.textureUnit);
                    break;
                case COLOR:
                    GL11.glColorPointer(vertexFormatElement.getCount(), k, i, vertexFormat.getIndex(j));
                    GL11.glEnableClientState(32886);
                    break;
                case NORMAL:
                    GL11.glNormalPointer(k, i, vertexFormat.getIndex(j));
                    GL11.glEnableClientState(32885);
            }
        }
	}

	public void renderQuad() {
		GL11.glDrawArrays(GL20C.GL_TRIANGLE_STRIP, 0, 4);
	}

	@SuppressWarnings("deprecation")
	public static void end() {
        VertexFormat vertexFormat = VertexFormats.POSITION_TEXTURE;
        int j = 0;
        List<VertexFormatElement> list = vertexFormat.getElements();
        for(int m = list.size(); j < m; ++j) {
            VertexFormatElement vertexFormatElement2 = (VertexFormatElement)list.get(j);
            VertexFormatElement.Type type2 = vertexFormatElement2.getType();
            int l = vertexFormatElement2.getIndex();
            switch (type2) {
                case POSITION:
                    GL11.glDisableClientState(32884);
                    break;
                case UV:
                    GLX.gl13ClientActiveTexture(GLX.textureUnit + l);
                    GL11.glDisableClientState(32888);
                    GLX.gl13ClientActiveTexture(GLX.textureUnit);
                    break;
                case COLOR:
                    GL11.glDisableClientState(32886);
                    GlStateManager.clearColor();
                    break;
                case NORMAL:
                    GL11.glDisableClientState(32885);
            }
        }		GL15.glBindBuffer(GL20C.GL_ARRAY_BUFFER, 0);

		RenderSystem.enableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.popMatrix();
	}

	/**
	 * Creates and uploads a vertex buffer containing a single full-screen quad
	 */
	private static int createQuad() {
		float[] vertices = new float[] {
			// Vertex 0: Top right corner
			1.0F, 1.0F, 0.0F,
			1.0F, 1.0F,
			// Vertex 1: Top left corner
			0.0F, 1.0F, 0.0F,
			0.0F, 1.0F,
			// Vertex 2: Bottom right corner
			1.0F, 0.0F, 0.0F,
			1.0F, 0.0F,
			// Vertex 3: Bottom left corner
			0.0F, 0.0F, 0.0F,
			0.0F, 0.0F
		};

		return IrisRenderSystem.bufferStorage(GL20C.GL_ARRAY_BUFFER, vertices, GL20C.GL_STATIC_DRAW);
	}
}
