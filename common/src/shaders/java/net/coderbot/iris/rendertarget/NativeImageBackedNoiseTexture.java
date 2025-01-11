package net.coderbot.iris.rendertarget;

import net.minecraft.client.texture.NativeImageBackedTexture;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Random;

public class NativeImageBackedNoiseTexture extends NativeImageBackedTexture {
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
}
