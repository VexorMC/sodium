package net.coderbot.iris.mixin.texture;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleTexture.class)
public interface SimpleTextureAccessor {
	@Accessor("location")
	Identifier getLocation();
}
