package net.coderbot.iris.mixin.texture;

import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResourceTexture.class)
public interface SimpleTextureAccessor {
	@Accessor("field_6555")
    Identifier getLocation();
}
