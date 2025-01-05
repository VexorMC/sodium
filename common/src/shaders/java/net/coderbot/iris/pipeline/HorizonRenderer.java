package net.coderbot.iris.pipeline;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Renders the sky horizon. Vanilla Minecraft simply uses the "clear color" for its horizon, and then draws a plane
 * above the player. This class extends the sky rendering so that an octagonal prism is drawn around the player instead,
 * allowing shaders to perform more advanced sky rendering.
 * <p>
 * However, the horizon rendering is designed so that when sky shaders are not being used, it looks almost exactly the
 * same as vanilla sky rendering, except a few almost entirely imperceptible differences where the walls
 * of the octagonal prism intersect the top plane.
 */
public class HorizonRenderer {
	/**
	 * The Y coordinate of the top skybox plane. Acts as the upper bound for the horizon prism, since the prism lies
	 * between the bottom and top skybox planes.
	 */
	private static final float TOP = 16.0F;

	/**
	 * The Y coordinate of the bottom skybox plane. Acts as the lower bound for the horizon prism, since the prism lies
	 * between the bottom and top skybox planes.
	 */
	private static final float BOTTOM = -16.0F;

	/**
	 * Cosine of 22.5 degrees.
	 */
	private static final double COS_22_5 = Math.cos(Math.toRadians(22.5));

	/**
	 * Sine of 22.5 degrees.
	 */
	private static final double SIN_22_5 = Math.sin(Math.toRadians(22.5));
    
    private static final int RADIUS = 384;
    
	private int currentRenderDistance;

	public HorizonRenderer() {
		currentRenderDistance = MinecraftClient.getInstance().options.viewDistance;

		rebuildBuffer();
	}

	private void rebuildBuffer() {

	}

	private void buildQuad(BufferBuilder consumer, double x1, double z1, double x2, double z2) {
		consumer.vertex(x1, BOTTOM, z1);
		consumer.next();
		consumer.vertex(x1, TOP, z1);
		consumer.next();
		consumer.vertex(x2, TOP, z2);
		consumer.next();
		consumer.vertex(x2, BOTTOM, z2);
		consumer.next();
	}

	private void buildHalf(BufferBuilder consumer, double adjacent, double opposite, boolean invert) {
		if (invert) {
			adjacent = -adjacent;
			opposite = -opposite;
		}

		// NB: Make sure that these vertices are being specified in counterclockwise order!
		// Otherwise back face culling will remove your quads, and you'll be wondering why there's a hole in your horizon.
		// Don't poke holes in the horizon. Specify vertices in counterclockwise order.

		// +X,-Z face
		buildQuad(consumer, adjacent, -opposite, opposite, -adjacent);
		// +X face
		buildQuad(consumer, adjacent, opposite, adjacent, -opposite);
		// +X,+Z face
		buildQuad(consumer, opposite, adjacent, adjacent, opposite);
		// +Z face
		buildQuad(consumer, -opposite, adjacent, opposite, adjacent);
	}

	/**
	 * @param adjacent the adjacent side length of the a triangle with a hypotenuse extending from the center of the
	 *                 octagon to a given vertex on the perimeter.
	 * @param opposite the opposite side length of the a triangle with a hypotenuse extending from the center of the
	 *                 octagon to a given vertex on the perimeter.
	 */
	private void buildOctagonalPrism(BufferBuilder consumer, double adjacent, double opposite) {
		buildHalf(consumer, adjacent, opposite, false);
		buildHalf(consumer, adjacent, opposite, true);
	}

	private void buildRegularOctagonalPrism(BufferBuilder consumer, double radius) {
		buildOctagonalPrism(consumer, radius * COS_22_5, radius * SIN_22_5);
	}

	private void buildBottomPlane(BufferBuilder consumer) {
		for (int x = -RADIUS; x <= RADIUS; x += 64) {
			for (int z = -RADIUS; z <= RADIUS; z += 64) {
				consumer.vertex(x + 64, BOTTOM, z);
				consumer.next();
				consumer.vertex(x, BOTTOM, z);
				consumer.next();
				consumer.vertex(x, BOTTOM, z + 64);
				consumer.next();
				consumer.vertex(x + 64, BOTTOM, z + 64);
				consumer.next();
			}
		}
	}

	private void buildTopPlane(BufferBuilder consumer) {
		// You might be tempted to try to combine this with buildBottomPlane to avoid code duplication,
		// but that won't work since the winding order has to be reversed or else one of the planes will be
		// discarded by back face culling.
		for (int x = -RADIUS; x <= RADIUS; x += 64) {
			for (int z = -RADIUS; z <= RADIUS; z += 64) {
				consumer.vertex(x + 64, TOP, z);
				consumer.next();
				consumer.vertex(x + 64, TOP, z + 64);
				consumer.next();
				consumer.vertex(x, TOP, z + 64);
				consumer.next();
				consumer.vertex(x, TOP, z);
				consumer.next();
			}
		}
	}

	private void buildHorizon(int radius, BufferBuilder consumer) {
		if (radius > 256) {
			// Prevent the prism from getting too large, this causes issues on some shader packs that modify the vanilla
			// sky if we don't do this.
			radius = 256;
		}

		buildRegularOctagonalPrism(consumer, radius);

		// Replicate the vanilla top plane since we can't assume that it'll be rendered.
		// TODO: Remove vanilla top plane
		buildTopPlane(consumer);

		// Always make the bottom plane have a radius of RADIUS, to match the top plane.
		buildBottomPlane(consumer);
	}

	public void renderHorizon() {
		if (currentRenderDistance != MinecraftClient.getInstance().options.viewDistance) {
			currentRenderDistance = MinecraftClient.getInstance().options.viewDistance;
			rebuildBuffer();
		}

        // TODO: Rather than rebuilding the buffer every frame, build it once at initialization
        
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION);

        buildHorizon(currentRenderDistance * 16, buffer);

        Tessellator.getInstance().draw();
	}

	public void destroy() {
	}
}
