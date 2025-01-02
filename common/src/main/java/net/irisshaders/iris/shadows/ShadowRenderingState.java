package net.irisshaders.iris.shadows;

import dev.vexor.radium.compat.mojang.math.PoseStack;

public class ShadowRenderingState {
	private static BlockEntityRenderFunction function = (ShadowRenderer::renderBlockEntities);

	public static boolean areShadowsCurrentlyBeingRendered() {
		return ShadowRenderer.ACTIVE;
	}

	public static void setBlockEntityRenderFunction(BlockEntityRenderFunction function) {
		ShadowRenderingState.function = function;
	}

	public static int renderBlockEntities(ShadowRenderer shadowRenderer, PoseStack modelView, double cameraX, double cameraY, double cameraZ, float tickDelta, boolean hasEntityFrustum, boolean lightsOnly) {
		return function.renderBlockEntities(shadowRenderer, modelView, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum, lightsOnly);
	}

	public static int getRenderDistance() {
		return ShadowRenderer.renderDistance;
	}

	public interface BlockEntityRenderFunction {
		int renderBlockEntities(ShadowRenderer shadowRenderer, PoseStack modelView, double cameraX, double cameraY, double cameraZ, float tickDelta, boolean hasEntityFrustum, boolean lightsOnly);
	}
}
