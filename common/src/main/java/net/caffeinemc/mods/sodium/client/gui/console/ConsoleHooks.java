package net.caffeinemc.mods.sodium.client.gui.console;

import net.caffeinemc.mods.sodium.client.console.Console;

public class ConsoleHooks {
    public static void render(double currentTime) {
        ConsoleRenderer.INSTANCE.update(Console.INSTANCE, currentTime);
        ConsoleRenderer.INSTANCE.draw();
    }
}
