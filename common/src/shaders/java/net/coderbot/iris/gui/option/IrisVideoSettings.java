package net.coderbot.iris.gui.option;

import net.coderbot.iris.Iris;

public class IrisVideoSettings {
	public static int shadowDistance = 32;

	public static int getOverriddenShadowDistance(int base) {
		return Iris.getPipelineManager().getPipeline()
				.map(pipeline -> pipeline.getForcedShadowRenderDistanceChunksForDisplay().orElse(base))
				.orElse(base);
	}

	public static boolean isShadowDistanceSliderEnabled() {
		return Iris.getPipelineManager().getPipeline()
				.map(pipeline -> !pipeline.getForcedShadowRenderDistanceChunksForDisplay().isPresent())
				.orElse(true);
	}
}
