package net.coderbot.iris.compat.sodium.mixin.pbr_animation;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.coderbot.iris.texture.pbr.PBRSpriteHolder;
import net.coderbot.iris.texture.pbr.TextureAtlasSpriteExtension;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlasSprite.class)
public class MixinTextureAtlasSprite {
	@Inject(method = "markActive()V", at = @At("TAIL"), remap = false)
	private void iris$onTailMarkActive(CallbackInfo ci) {
		PBRSpriteHolder pbrHolder = ((TextureAtlasSpriteExtension) this).getPBRHolder();
		if (pbrHolder != null) {
			TextureAtlasSprite normalSprite = pbrHolder.getNormalSprite();
			TextureAtlasSprite specularSprite = pbrHolder.getSpecularSprite();
			if (normalSprite != null) {
				SpriteUtil.markSpriteActive(normalSprite);
			}
			if (specularSprite != null) {
				SpriteUtil.markSpriteActive(specularSprite);
			}
		}
	}
}
