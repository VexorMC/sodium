package net.caffeinemc.mods.sodium.client.util;

public class BlockRenderType {
    public static boolean isInvisible(int renderType) {
        return renderType == -1;
    }

    public static boolean isLiquid(int renderType) {
        return renderType == 1;
    }

    public static boolean isBlockEnderChest(int renderType) {
        return renderType == 2;
    }

    public static boolean isModel(int renderType) {
        return renderType == 3;
    }
}
