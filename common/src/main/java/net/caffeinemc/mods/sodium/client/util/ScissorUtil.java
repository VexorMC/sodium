package net.caffeinemc.mods.sodium.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;

public class ScissorUtil {
    public static void scissor(int x, int y, int width, int height) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = new Window(client);
        int scale = window.getScaleFactor();

        GL11.glScissor(
                x * scale,
                (client.height - (y + height) * scale),
                width * scale,
                height * scale
        );
    }

    public static void withScissor(int x, int y, int width, int height, Runnable action) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        scissor(x, y, width, height);
        action.run();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
