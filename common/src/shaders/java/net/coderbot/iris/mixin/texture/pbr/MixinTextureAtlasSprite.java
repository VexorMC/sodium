package net.coderbot.iris.mixin.texture.pbr;

import net.coderbot.iris.texture.pbr.PBRSpriteHolder;
import net.coderbot.iris.texture.pbr.TextureAtlasSpriteExtension;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sprite.class)
public class MixinTextureAtlasSprite implements TextureAtlasSpriteExtension {
	@Unique
	private PBRSpriteHolder pbrHolder;

	@Override
	public PBRSpriteHolder getPBRHolder() {
		return pbrHolder;
	}

	@Override
	public PBRSpriteHolder getOrCreatePBRHolder() {
		if (pbrHolder == null) {
			pbrHolder = new PBRSpriteHolder();
		}
		return pbrHolder;
	}
}
