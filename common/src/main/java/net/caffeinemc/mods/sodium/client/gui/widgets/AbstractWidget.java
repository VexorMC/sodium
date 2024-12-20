package net.caffeinemc.mods.sodium.client.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiParentEventListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public abstract class AbstractWidget implements Renderable, GuiEventListener {
    protected final TextRenderer font;
    protected boolean focused;
    protected boolean hovered;

    protected AbstractWidget() {
        this.font = MinecraftClient.getInstance().textRenderer;
    }

    protected void drawString(String text, int x, int y, int color) {
        font.draw(text, x, y, color);
    }

    protected void drawString(Text text, int x, int y, int color) {
        font.draw(text.asFormattedString(), x, y, color);
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

    protected int getStringWidth(Text text) {
        return this.font.getStringWidth(text.asFormattedString());
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    protected void drawBorder(int x1, int y1, int x2, int y2, int color) {
        DrawableHelper.fill(x1, y1, x2, y1 + 1, color);
        DrawableHelper.fill(x1, y2 - 1, x2, y2, color);
        DrawableHelper.fill(x1, y1, x1 + 1, y2, color);
        DrawableHelper.fill(x2 - 1, y1, x2, y2, color);
    }

    protected void drawQuads(Consumer<BufferBuilder> consumer) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);

        consumer.accept(bufferBuilder);

        tessellator.draw();

        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    protected static void addQuad(BufferBuilder consumer, double x1, double y1, double x2, double y2, float a, float r, float g, float b) {
        consumer.vertex(x2, y1, 0.0D).color(r, g, b, a).next();
        consumer.vertex(x1, y1, 0.0D).color(r, g, b, a).next();
        consumer.vertex(x1, y2, 0.0D).color(r, g, b, a).next();
        consumer.vertex(x2, y2, 0.0D).color(r, g, b, a).next();
    }

    @Override
    public abstract boolean isMouseOver(double x, double y);

    protected int getTextWidth(Text text) {
        return this.font.getStringWidth(text.asFormattedString());
    }
}
