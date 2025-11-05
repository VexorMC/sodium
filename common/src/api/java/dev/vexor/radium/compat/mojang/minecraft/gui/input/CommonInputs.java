package dev.vexor.radium.compat.mojang.minecraft.gui.input;

import static org.lwjgl.glfw.GLFW.*;

public class CommonInputs {
    public static boolean selected(int key) {
        return key == GLFW_KEY_ENTER || key == GLFW_KEY_SPACE || key == GLFW_KEY_KP_ENTER;
    }
}