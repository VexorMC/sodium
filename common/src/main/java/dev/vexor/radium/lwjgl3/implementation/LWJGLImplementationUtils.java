package dev.vexor.radium.lwjgl3.implementation;

import dev.vexor.radium.lwjgl3.implementation.glfw.GLFWKeyboardImplementation;
import dev.vexor.radium.lwjgl3.implementation.glfw.GLFWMouseImplementation;
import dev.vexor.radium.lwjgl3.implementation.input.CombinedInputImplementation;
import dev.vexor.radium.lwjgl3.implementation.input.InputImplementation;
import dev.vexor.radium.lwjgl3.implementation.input.MouseImplementation;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 3:12 PM
 */
public class LWJGLImplementationUtils {
    private static final boolean allowVirtualCursor = Boolean.getBoolean("legacy_lwjgl3.allow_virtual_cursor") || System.getenv("LEGACY_LWJGL3_ALLOW_VIRTUAL_CURSOR") != null;
    private static InputImplementation _inputImplementation;

    public static InputImplementation getOrCreateInputImplementation() {
        if (_inputImplementation == null) {
            _inputImplementation = createImplementation();
        }
        return _inputImplementation;
    }

    private static InputImplementation createImplementation() {
        MouseImplementation mouse = new GLFWMouseImplementation();
        return new CombinedInputImplementation(new GLFWKeyboardImplementation(), mouse);
    }

}
