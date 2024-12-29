package dev.vexor.radium.compat.mojang.minecraft.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class FogHelper {
    public static float getFogEnd() {
        return GlStateManager.FOG.end;
    }

    public static float getFogStart() {
        return GlStateManager.FOG.start;
    }

    public static float[] getFogColor() {
        GameRenderer gameRenderer = MinecraftClient.getInstance().gameRenderer;
        return new float[]{gameRenderer.fogRed, gameRenderer.fogGreen, gameRenderer.fogBlue, 1.0f};
    }
}