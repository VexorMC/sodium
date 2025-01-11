package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

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

    public void begin() {
        GlStateManager.disableDepthTest();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        // scale the quad from [0, 1] to [-1, 1]
        GL11.glTranslatef(-1.0F, -1.0F, 0.0F);
        GL11.glScalef(2.0F, 2.0F, 0.0F);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderQuad() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadBuffer);

        GL11.glVertexPointer(3, GL11.GL_FLOAT, 5 * Float.BYTES, 0); // 3 floats for position, stride 5 * float size, offset 0
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 5 * Float.BYTES, 3 * Float.BYTES); // 2 floats for texture coords, stride 5 * float size, offset 3 * float size
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public static void end() {
        GlStateManager.enableDepthTest();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
    }

    /**
     * Creates and uploads a vertex buffer containing a single full-screen quad
     */
    private static int createQuad() {
        return IrisRenderSystem.bufferStorage(GL15.GL_ARRAY_BUFFER, new float[]{
                // Vertex 0: Top right corner
                1.0F, 1.0F, 0.0F, 1.0F, 1.0F,
                // Vertex 1: Top left corner
                0.0F, 1.0F, 0.0F, 0.0F, 1.0F,
                // Vertex 2: Bottom right corner
                1.0F, 0.0F, 0.0F, 1.0F, 0.0F,
                // Vertex 3: Bottom left corner
                0.0F, 0.0F, 0.0F, 0.0F, 0.0F
        }, GL15.GL_STATIC_DRAW);
    }
}