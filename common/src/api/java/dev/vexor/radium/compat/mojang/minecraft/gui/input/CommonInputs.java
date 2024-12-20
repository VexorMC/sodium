package dev.vexor.radium.compat.mojang.minecraft.gui.input;

public class CommonInputs {
    public static boolean selected(int n) {
        return n == 257 || n == 32 || n == 335;
    }
}