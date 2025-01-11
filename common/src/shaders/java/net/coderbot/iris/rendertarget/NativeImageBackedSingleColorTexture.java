package net.coderbot.iris.rendertarget;

import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.awt.image.BufferedImage;

public class NativeImageBackedSingleColorTexture extends NativeImageBackedTexture {
	public NativeImageBackedSingleColorTexture(int red, int green, int blue, int alpha) {
		super(create(ColorARGB.pack(alpha, blue, green, red)));
	}

	public NativeImageBackedSingleColorTexture(int rgba) {
		this(rgba >> 24 & 0xFF, rgba >> 16 & 0xFF, rgba >> 8 & 0xFF, rgba & 0xFF);
	}

	private static BufferedImage create(int color) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		image.setRGB(0, 0, color);

		return image;
	}
}
