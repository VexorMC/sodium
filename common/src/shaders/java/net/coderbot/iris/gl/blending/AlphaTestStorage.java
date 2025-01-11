package net.coderbot.iris.gl.blending;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.mixin.statelisteners.BooleanStateAccessor;

public class AlphaTestStorage {
	private static boolean originalAlphaTestEnable;
	private static AlphaTest originalAlphaTest;
	private static boolean alphaTestLocked;

	public static boolean isAlphaTestLocked() {
		return alphaTestLocked;
	}

	public static void overrideAlphaTest(AlphaTest override) {
		if (!alphaTestLocked) {
			// Only save the previous state if the alpha test wasn't already locked
			GlStateManager.AlphaTestState alphaState = GlStateManagerAccessor.getALPHA_TEST();

			originalAlphaTestEnable = ((BooleanStateAccessor) alphaState.capState).isEnabled();
			originalAlphaTest = new AlphaTest(AlphaTestFunction.fromGlId(alphaState.func).get(), alphaState.ref);
		}

		alphaTestLocked = false;

		if (override == null) {
			GlStateManager.disableAlphaTest();
		} else {
			GlStateManager.enableAlphaTest();
			GlStateManager.alphaFunc(override.getFunction().getGlId(), override.getReference());
		}

		alphaTestLocked = true;
	}

	public static void deferAlphaTestToggle(boolean enabled) {
		originalAlphaTestEnable = enabled;
	}

	public static void deferAlphaFunc(int function, float reference) {
		originalAlphaTest = new AlphaTest(AlphaTestFunction.fromGlId(function).get(), reference);
	}

	public static void restoreAlphaTest() {
		if (!alphaTestLocked) {
			return;
		}

		alphaTestLocked = false;

		if (originalAlphaTestEnable) {
			GlStateManager.enableAlphaTest();
		} else {
			GlStateManager.disableAlphaTest();
		}

		GlStateManager.alphaFunc(originalAlphaTest.getFunction().getGlId(), originalAlphaTest.getReference());
	}
}
