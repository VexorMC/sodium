package com.cloverclient.lwjgl3.implementation;

import org.lwjgl.opengl.InputImplementation;

import com.cloverclient.lwjgl3.implementation.glfw.GLFWKeyboardImplementation;
import com.cloverclient.lwjgl3.implementation.glfw.GLFWMouseImplementation;
import com.cloverclient.lwjgl3.implementation.input.CombinedInputImplementation;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 3:12 PM
 */
public class LWJGLImplementationUtils {
    private static InputImplementation _inputImplementation;

    public static InputImplementation getOrCreateInputImplementation() {
        if (_inputImplementation == null) {
            _inputImplementation = createImplementation();
        }
        return _inputImplementation;
    }

    private static InputImplementation createImplementation() {
        return new CombinedInputImplementation(new GLFWKeyboardImplementation(), new GLFWMouseImplementation());
    }

}
