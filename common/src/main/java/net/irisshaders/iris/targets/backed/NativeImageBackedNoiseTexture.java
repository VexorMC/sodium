package net.irisshaders.iris.targets.backed;

import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.gl.texture.TextureType;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Random;
import java.util.function.IntSupplier;

public class NativeImageBackedNoiseTexture extends NativeImageBackedTexture implements TextureAccess {
	public NativeImageBackedNoiseTexture(int size) {
		super(create(size));
	}

	private static BufferedImage create(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Random random = new Random(0);

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				int color = random.nextInt() | (255 << 24);

				image.setRGB(x, y, color);
			}
		}

		return image;
	}

	@Override
	public TextureType getType() {
		return TextureType.TEXTURE_2D;
	}

	@Override
	public IntSupplier getTextureId() {
		return this::getGlId;
	}
}
