package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GlStateManager.class, remap = false)
public interface GlStateManagerAccessor {
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

	@Accessor("activeTexture")
	static int getActiveTexture() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("TEXTURES")
	static GlStateManager.Texture2DState[] getTEXTURES() {
		throw new UnsupportedOperationException("Not accessed");
	}
}
