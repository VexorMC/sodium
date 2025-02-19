package dev.vexor.radium.compat.mojang.minecraft;

import net.minecraft.world.chunk.ChunkNibbleArray;

import java.util.Arrays;

public class ChunkNibbleArrayExt extends ChunkNibbleArray {
    public ChunkNibbleArrayExt(int value) {
        super(fill(value));
    }

    private static byte[] fill(int value) {
        if ((value & ~15) != 0)
            throw new IllegalArgumentException("Value must be a nibble (0-15), got: " + value);

        byte packed = (byte) ((value & 15) | ((value & 15) << 4));
        byte[] array = new byte[2048];
        Arrays.fill(array, packed);
        return array;
    }
}
