package net.irisshaders.iris.fantastic;

import net.caffeinemc.mods.sodium.client.gui.options.named.GraphicsMode;
import net.irisshaders.iris.Iris;

public enum SupportedGraphicsMode {
	FAST,
	FANCY;

	public static SupportedGraphicsMode fromVanilla(GraphicsMode status) {
		return switch (status) {
			case FAST -> FAST;
			case FANCY -> FANCY;
		};
	}

	public GraphicsMode toVanilla() {
		return switch (this) {
			case FAST -> GraphicsMode.FAST;
			case FANCY -> GraphicsMode.FANCY;
		};
	}
}
