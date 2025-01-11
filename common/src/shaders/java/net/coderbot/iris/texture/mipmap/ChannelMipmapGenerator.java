package net.coderbot.iris.texture.mipmap;

import net.caffeinemc.mods.sodium.api.util.ColorARGB;

public class ChannelMipmapGenerator extends AbstractMipmapGenerator {
	protected final BlendFunction redFunc;
	protected final BlendFunction greenFunc;
	protected final BlendFunction blueFunc;
	protected final BlendFunction alphaFunc;

	public ChannelMipmapGenerator(BlendFunction redFunc, BlendFunction greenFunc, BlendFunction blueFunc, BlendFunction alphaFunc) {
		this.redFunc = redFunc;
		this.greenFunc = greenFunc;
		this.blueFunc = blueFunc;
		this.alphaFunc = alphaFunc;
	}

	@Override
	public int blend(int c0, int c1, int c2, int c3) {
		return ColorARGB.pack(
				alphaFunc.blend(
                        ColorARGB.unpackAlpha(c0),
                        ColorARGB.unpackAlpha(c1),
						ColorARGB.unpackAlpha(c2),
						ColorARGB.unpackAlpha(c3)
				),
				blueFunc.blend(
						ColorARGB.unpackBlue(c0),
						ColorARGB.unpackBlue(c1),
						ColorARGB.unpackBlue(c2),
						ColorARGB.unpackBlue(c3)
				),
				greenFunc.blend(
						ColorARGB.unpackGreen(c0),
						ColorARGB.unpackGreen(c1),
						ColorARGB.unpackGreen(c2),
						ColorARGB.unpackGreen(c3)
				),
				redFunc.blend(
						ColorARGB.unpackRed(c0),
						ColorARGB.unpackRed(c1),
						ColorARGB.unpackRed(c2),
						ColorARGB.unpackRed(c3)
				)
		);
	}

	public interface BlendFunction {
		int blend(int v0, int v1, int v2, int v3);
	}
}
