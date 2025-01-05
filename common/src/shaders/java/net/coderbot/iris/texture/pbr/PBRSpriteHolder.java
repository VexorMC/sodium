package net.coderbot.iris.texture.pbr;

import net.minecraft.client.texture.Sprite;
import org.jetbrains.annotations.Nullable;

public class PBRSpriteHolder {
	protected Sprite normalSprite;
	protected Sprite specularSprite;

	@Nullable
	public Sprite getNormalSprite() {
		return normalSprite;
	}

	@Nullable
	public Sprite getSpecularSprite() {
		return specularSprite;
	}

	public void setNormalSprite(Sprite sprite) {
		normalSprite = sprite;
	}

	public void setSpecularSprite(Sprite sprite) {
		specularSprite = sprite;
	}

	public void close() {
	}
}
