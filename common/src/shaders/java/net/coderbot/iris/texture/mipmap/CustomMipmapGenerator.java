package net.coderbot.iris.texture.mipmap;

import com.mojang.blaze3d.platform.NativeImage;
import net.coderbot.iris.texture.pbr.loader.SpriteInfo;
import org.jetbrains.annotations.Nullable;

public interface CustomMipmapGenerator {
	NativeImage[] generateMipLevels(NativeImage image, int mipLevel);

	public interface Provider {
		@Nullable
		CustomMipmapGenerator getMipmapGenerator(SpriteInfo info, int atlasWidth, int atlasHeight);
	}
}
