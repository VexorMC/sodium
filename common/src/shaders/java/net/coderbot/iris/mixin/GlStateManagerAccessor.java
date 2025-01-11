package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlStateManager.class)
public interface GlStateManagerAccessor {
	@Accessor("activeTexture")
	static int getActiveTexture() {
		throw new AssertionError();
	}

	@Accessor("ALPHA_TEST")
	static GlStateManager.AlphaTestState getALPHA_TEST() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("BLEND")
	static GlStateManager.BlendFuncState getBLEND() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("COLOR_MASK")
	static GlStateManager.ColorMask getCOLOR_MASK() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("DEPTH")
	static GlStateManager.DepthTestState getDEPTH() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("FOG")
	static GlStateManager.FogState getFOG() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("TEXTURES")
	static GlStateManager.Texture2DState[] getTEXTURES() {
		throw new UnsupportedOperationException("Not accessed");
	}
}
