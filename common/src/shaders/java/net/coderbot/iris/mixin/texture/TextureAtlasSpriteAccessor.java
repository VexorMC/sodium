package net.coderbot.iris.mixin.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.resource.AnimationMetadata;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Sprite.class)
public interface TextureAtlasSpriteAccessor {
	@Accessor("meta")
    AnimationMetadata getMetadata();

    @Accessor("field_11198")
    int[][] getMainImage();

    @Accessor("frames")
    List<int[][]> getFrames();

	@Accessor("x")
	int getX();

	@Accessor("y")
	int getY();

	@Accessor("frameIndex")
	int getFrame();

	@Accessor("frameIndex")
	void setFrame(int frame);
}
