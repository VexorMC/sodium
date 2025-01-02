package net.irisshaders.iris.targets.backed;

import dev.vexor.radium.compat.mojang.minecraft.FastColor;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.awt.image.BufferedImage;

public class NativeImageBackedSingleColorTexture extends NativeImageBackedTexture {
	public NativeImageBackedSingleColorTexture(int red, int green, int blue, int alpha) {
		super(create(FastColor.ABGR32.color(alpha, blue, green, red)));
	}

	public NativeImageBackedSingleColorTexture(int rgba) {
		this(rgba >> 24 & 0xFF, rgba >> 16 & 0xFF, rgba >> 8 & 0xFF, rgba & 0xFF);
	}

	private static BufferedImage create(int color) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

		image.setRGB(0, 0, color);

		return image;
	}
}
