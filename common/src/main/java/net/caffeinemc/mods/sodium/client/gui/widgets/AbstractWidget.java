package net.caffeinemc.mods.sodium.client.gui.widgets;

import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.caffeinemc.mods.sodium.client.gui.Dimensioned;
import net.caffeinemc.mods.sodium.client.util.Dim2i;

public abstract class AbstractWidget extends DrawableHelper implements Renderable, GuiEventListener, Dimensioned {
    protected final TextRenderer font = MinecraftClient.getInstance().textRenderer;
    private final Dim2i dim;
    protected boolean focused;
    protected boolean hovered;

    protected AbstractWidget(Dim2i dim) {
        this.dim = dim;
    }

    @Override
    public Dim2i getDimensions() {
        return this.dim;
    }

    protected void drawString(String text, int x, int y, int color) {
        this.font.draw(text, x, y, color);
    }

    protected void drawString(Text text, int x, int y, int color) {
        this.font.draw(text.asFormattedString(), x, y, color);
    }

    protected void drawCenteredString(Text text, int x, int y, int color) {
//        graphics.drawCenteredString(this.font, text, x, y, color);
    }

    public boolean isHovered() {
        return this.hovered;
    }

    protected void drawRect(int x1, int y1, int x2, int y2, int color) {
        DrawableHelper.fill(x1, y1, x2, y2, color);
    }

    protected void playClickSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(new Identifier("gui.button.press"), 1.0F));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.getX() && mouseX < this.getLimitX() && mouseY >= this.getY() && mouseY < this.getLimitY();
    }

    protected int getStringWidth(Text text) {
        return this.font.getStringWidth(text.asFormattedString());
    }


    @Override
    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(boolean focused) {
        if (!focused) {
            this.focused = false;
        }
    }

    protected String truncateTextToFit(String name, int targetWidth) {
        var suffix = "...";
        var suffixWidth = this.font.getStringWidth(suffix);
        var nameFontWidth = this.font.getStringWidth(name);
        if (nameFontWidth > targetWidth) {
            targetWidth -= suffixWidth;
            int maxLabelChars = name.length() - 3;
            int minLabelChars = 1;

            // binary search on how many chars fit
            while (maxLabelChars - minLabelChars > 1) {
                var mid = (maxLabelChars + minLabelChars) / 2;
                var midName = name.substring(0, mid);
                var midWidth = this.font.getStringWidth(midName);
                if (midWidth > targetWidth) {
                    maxLabelChars = mid;
                } else {
                    minLabelChars = mid;
                }
            }

            name = name.substring(0, minLabelChars).trim() + suffix;
        }
        return name;
    }

    protected void drawBorder(int x1, int y1, int x2, int y2, int color) {
        DrawableHelper.fill(x1, y1, x2, y1 + 1, color);
        DrawableHelper.fill(x1, y2 - 1, x2, y2, color);
        DrawableHelper.fill(x1, y1, x1 + 1, y2, color);
        DrawableHelper.fill(x2 - 1, y1, x2, y2, color);
    }
}
