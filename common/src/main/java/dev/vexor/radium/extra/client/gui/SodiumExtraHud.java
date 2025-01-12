package dev.vexor.radium.extra.client.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import dev.vexor.radium.mixin.extra.gui.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.Window;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SodiumExtraHud {

    private final List<Text> textList = new ObjectArrayList<>();

    private final MinecraftClient client = MinecraftClient.getInstance();

    public void onStartTick(MinecraftClient client) {
        // Clear the textList to start fresh (this might not be ideal but hey it's still better than whatever the fuck debug hud is doing)
        this.textList.clear();
        if (SodiumExtraClientMod.options().extraSettings.showFps) {
            int currentFPS = MinecraftClient.getCurrentFps();

            Text text = new TranslatableText("sodium-extra.overlay.fps", currentFPS);

            if (SodiumExtraClientMod.options().extraSettings.showFPSExtended)
                text = new LiteralText(String.format("%s %s", text.asFormattedString(), new TranslatableText("sodium-extra.overlay.fps_extended", SodiumExtraClientMod.getClientTickHandler().getHighestFps(), SodiumExtraClientMod.getClientTickHandler().getAverageFps(),
                        SodiumExtraClientMod.getClientTickHandler().getLowestFps()).asFormattedString()));

            this.textList.add(text);
        }

        if (SodiumExtraClientMod.options().extraSettings.showCoords && this.client.player != null) {
            Vec3d pos = this.client.player.getPos();

            TranslatableText text = new TranslatableText("sodium-extra.overlay.coordinates", String.format("%.2f", pos.x), String.format("%.2f", pos.y), String.format("%.2f", pos.z));
            this.textList.add(text);
        }
    }

    public void onHudRender() {
        if (this.client.options.debugEnabled && !this.client.options.hudHidden) {
            SodiumExtraGameOptions.OverlayCorner overlayCorner = SodiumExtraClientMod.options().extraSettings.overlayCorner;
            // Calculate starting position based on the overlay corner
            int x;
            int y = overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_LEFT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT ?
                    new Window(this.client).getHeight() - this.client.textRenderer.fontHeight - 2 : 2;
            // Render each text in the list
            for (Text text : this.textList) {
                if (overlayCorner == SodiumExtraGameOptions.OverlayCorner.TOP_RIGHT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT) {
                    x = new Window(this.client).getWidth() - this.client.textRenderer.getStringWidth(text.asFormattedString()) - 2;
                } else {
                    x = 2;
                }
                this.drawString(text, x, y);
                if (overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_LEFT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT) {
                    y -= client.textRenderer.fontHeight + 2;
                } else {
                    y += client.textRenderer.fontHeight + 2; // Increase the y-position for the next text
                }
            }
        }
    }

    private void drawString(Text text, int x, int y) {
        int textColor = 0xffffffff; // Default text color

        if (SodiumExtraClientMod.options().extraSettings.textContrast == SodiumExtraGameOptions.TextContrast.BACKGROUND) {
            DrawableHelper.fill(x - 1, y - 1, x + this.client.textRenderer.getStringWidth(text.asFormattedString()) + 1, y + this.client.textRenderer.fontHeight + 1, -1873784752);
        }

        this.client.textRenderer.draw(text.asFormattedString(), x, y, textColor, SodiumExtraClientMod.options().extraSettings.textContrast == SodiumExtraGameOptions.TextContrast.SHADOW);
    }
}
