package net.irisshaders.iris.mixin.texture;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(SpriteAtlasTexture.class)
public interface TextureAtlasAccessor {
	@Accessor("sprites")
	Map<String, Sprite> getTexturesByName();

	@Accessor("maxTextureSize")
	int getMipLevel();

	@Invoker("getWidth")
	int callGetWidth();

	@Invoker("getHeight")
	int callGetHeight();
}
