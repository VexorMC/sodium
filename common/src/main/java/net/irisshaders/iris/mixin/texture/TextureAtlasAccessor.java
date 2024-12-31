package net.irisshaders.iris.mixin.texture;

import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(SpriteAtlasTexture.class)
public interface TextureAtlasAccessor {
	@Accessor("texturesByName")
	Map<ResourceLocation, TextureAtlasSprite> getTexturesByName();

	@Accessor("maxTextureSize")
	int getMipLevel();

	@Invoker("getWidth")
	int callGetWidth();

	@Invoker("getHeight")
	int callGetHeight();
}
