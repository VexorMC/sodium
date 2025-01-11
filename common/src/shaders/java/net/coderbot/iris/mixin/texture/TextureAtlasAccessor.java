package net.coderbot.iris.mixin.texture;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {
	@Accessor("texturesByName")
	Map<Identifier, TextureAtlasSprite> getTexturesByName();

	@Invoker("getIdentifier")
	Identifier callGetIdentifier(Identifier location);
}
