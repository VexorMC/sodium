package net.coderbot.iris.texture.mipmap;

import org.jetbrains.annotations.Nullable;

public interface CustomMipmapGenerator {
    int[][] generateMipLevels(NativeImage image, int mipLevel);

	public interface Provider {
		@Nullable
		CustomMipmapGenerator getMipmapGenerator(TextureAtlasSprite.Info info, int atlasWidth, int atlasHeight);
	}
}
