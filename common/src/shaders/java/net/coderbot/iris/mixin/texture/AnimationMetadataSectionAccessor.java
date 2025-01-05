package net.coderbot.iris.mixin.texture;

import net.minecraft.client.resource.AnimationMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnimationMetadata.class)
public interface AnimationMetadataSectionAccessor {
	@Accessor("width")
	int getFrameWidth();

	@Mutable
	@Accessor("width")
	void setFrameWidth(int frameWidth);

	@Accessor("height")
	int getFrameHeight();

	@Mutable
	@Accessor("height")
	void setFrameHeight(int frameHeight);
}
