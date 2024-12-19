package dev.lunasa.compat.mojang.minecraft.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class FogHelper {
    private static final float FAR_PLANE_THRESHOLD_EXP = (float) Math.log(1.0f / 0.0019f);
    private static final float FAR_PLANE_THRESHOLD_EXP2 = MathHelper.sqrt(FAR_PLANE_THRESHOLD_EXP);

    private static GlStateManager.FogState fogState() {
        return GlStateManager.FOG;
    }

    public static float getFogEnd() {
        return fogState().end;
    }

    public static float getFogStart() {
        return fogState().start;
    }

    public static float getFogDensity() {
        return fogState().density;
    }

    public static float getFogCutoff() {
        int mode = fogState().mode;

        return switch (mode) {
            case GL11.GL_LINEAR -> getFogEnd();
            case GL11.GL_EXP -> FAR_PLANE_THRESHOLD_EXP / getFogDensity();
            case GL11.GL_EXP2 -> FAR_PLANE_THRESHOLD_EXP2 / getFogDensity();
            default -> 0.0f;
        };
    }

    public static float[] getFogColor() {
        GameRenderer gameRenderer = MinecraftClient.getInstance().gameRenderer;
        return new float[]{gameRenderer.fogRed, gameRenderer.fogGreen, gameRenderer.fogBlue, 1.0f};
    }
}