package net.coderbot.iris.mixin.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Sprite.class)
public class MixinTextureAtlasSprite {
}
