package net.irisshaders.iris.gl.blending;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;

public class DepthColorStorage {
	private static boolean originalDepthEnable;
	private static ColorMask originalColor;
	private static boolean depthColorLocked;

	public static boolean isDepthColorLocked() {
		return depthColorLocked;
	}

	public static void disableDepthColor() {
		if (!depthColorLocked) {
			// Only save the previous state if the depth and color mask wasn't already locked
			GlStateManager.ColorMask colorMask = GlStateManagerAccessor.getCOLOR_MASK();
			GlStateManager.DepthTestState depthState = GlStateManagerAccessor.getDEPTH();

			originalDepthEnable = depthState.mask;
			originalColor = new ColorMask(colorMask.red, colorMask.green, colorMask.blue, colorMask.alpha);
		}

		depthColorLocked = false;

		GlStateManager.depthMask(false);
		GlStateManager.colorMask(false, false, false, false);

		depthColorLocked = true;
	}

	public static void deferDepthEnable(boolean enabled) {
		originalDepthEnable = enabled;
	}

	public static void deferColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		originalColor = new ColorMask(red, green, blue, alpha);
	}

	public static void unlockDepthColor() {
		if (!depthColorLocked) {
			return;
		}

		depthColorLocked = false;

		GlStateManager.depthMask(originalDepthEnable);

		GlStateManager.colorMask(originalColor.isRedMasked(), originalColor.isGreenMasked(), originalColor.isBlueMasked(), originalColor.isAlphaMasked());
	}
}
