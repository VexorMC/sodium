package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Sprite.class)
public class MixinTextureAtlasSprite {
	@Redirect(method = "method_7013", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureUtil;method_7021(II[[I)[[I"))
	private int[][] iris$redirectMipmapGeneration(int i, int j, int[][] is) {
		if (this instanceof CustomMipmapGenerator.Provider) {
			CustomMipmapGenerator.Provider provider = (CustomMipmapGenerator.Provider) this;
			CustomMipmapGenerator generator = provider.getMipmapGenerator(info, atlasWidth, atlasHeight);
			if (generator != null) {
				return generator.generateMipLevels(nativeImage, mipLevel);
			}
		}
		return MipmapGenerator.generateMipLevels(nativeImage, mipLevel);
	}
}
