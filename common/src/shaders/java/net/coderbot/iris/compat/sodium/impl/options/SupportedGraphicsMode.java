package net.coderbot.iris.compat.sodium.impl.options;

public enum SupportedGraphicsMode {
	FAST, FANCY;

	public static SupportedGraphicsMode fromVanilla(boolean vanilla) {
		if (!vanilla) {
			return FAST;
		} else {
			return FANCY;
		}
	}

	public boolean toVanilla() {
		if (this == FAST) {
			return false;
		} else {
			return true;
		}
	}
}
