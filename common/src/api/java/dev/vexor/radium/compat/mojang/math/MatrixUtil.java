package dev.vexor.radium.compat.mojang.math;

import org.joml.Matrix4f;

public class MatrixUtil {
    public static boolean isPureTranslation(Matrix4f matrix4f) {
        return (matrix4f.properties() & 8) != 0;
    }

    public static boolean isOrthonormal(Matrix4f matrix4f) {
        return (matrix4f.properties() & 0x10) != 0;
    }
}