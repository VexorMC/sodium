package net.coderbot.iris.mixin.texture;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(SpriteAtlasTexture.class)
public interface TextureAtlasAccessor {
	@Accessor("sprites")
	Map<String, Sprite> getTexturesByName();

    @Invoker("method_7003")
    Identifier completeResourceLocation(Identifier location, int i);
}
