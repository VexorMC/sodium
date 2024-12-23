package net.caffeinemc.mods.sodium.client.gui.prompt;

import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ScreenPrompt implements GuiEventListener, Renderable {
    private final ScreenPromptable parent;
    private final List<String> text;

    private final Action action;

    private FlatButtonWidget closeButton, actionButton;

    private final int width, height;

    public ScreenPrompt(ScreenPromptable parent, List<String> text, int width, int height, Action action) {
        this.parent = parent;
        this.text = text;

        this.width = width;
        this.height = height;

        this.action = action;
    }

    public void init() {
        var parentDimensions = this.parent.getDimensions();

        int boxX = (parentDimensions.width() / 2) - (width / 2);
        int boxY = (parentDimensions.height() / 2) - (height / 2);

        this.closeButton = new FlatButtonWidget(new Dim2i((boxX + width) - 84, (boxY + height) - 24, 80, 20), new LiteralText("Close"), this::close);
        this.closeButton.setStyle(createButtonStyle());

        this.actionButton = new FlatButtonWidget(new Dim2i((boxX + width) - 198, (boxY + height) - 24, 110, 20), this.action.label, this::runAction);
        this.actionButton.setStyle(createButtonStyle());
    }

    public void render(int mouseX, int mouseY, float delta) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, 0.0f, 1000.0f);

        var parentDimensions = this.parent.getDimensions();

        DrawableHelper.fill(0, 0, parentDimensions.width(), parentDimensions.height(), 0x70090909);

        GL11.glTranslatef(0.0f, 0.0f, 50.0f);

        int boxX = (parentDimensions.width() / 2) - (width / 2);
        int boxY = (parentDimensions.height() / 2) - (height / 2);

        DrawableHelper.fill(boxX, boxY, boxX + width, boxY + height, 0xFF171717);

        GL11.glTranslatef(0.0f, 0.0f, 50.0f);

        int padding = 5;

        int textX = boxX + padding;
        int textY = boxY + padding;

        int textMaxWidth = width - (padding * 2);
        int textMaxHeight = height - (padding * 2);

        var font = MinecraftClient.getInstance().textRenderer;

        for (var paragraph : this.text) {
            var formatted = font.wrapLines(paragraph, textMaxWidth);

            for (var line : formatted) {
                font.draw(line, textX, textY, 0xFFFFFFFF, true);
                textY += font.fontHeight + 2;
            }

            textY += 8;
        }

        for (var button : getWidgets()) {
            button.render(mouseX, mouseY, delta);
        }

        GL11.glPopMatrix();
    }

    private static FlatButtonWidget.Style createButtonStyle() {
        var style = new FlatButtonWidget.Style();
        style.bgDefault = 0xff2b2b2b;
        style.bgHovered = 0xff393939;
        style.bgDisabled = style.bgDefault;

        style.textDefault = 0xFFFFFFFF;
        style.textDisabled = style.textDefault;

        return style;
    }

    @NotNull
    public List<AbstractWidget> getWidgets() {
        return List.of(this.actionButton, this.closeButton);
    }

    @Override
    public void setFocused(boolean focused) {
        if (focused) {
            this.parent.setPrompt(this);
        } else {
            this.parent.setPrompt(null);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (var widget : this.getWidgets()) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, char scanCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.close();
            return true;
        }

        return GuiEventListener.super.keyPressed(keyCode, scanCode);
    }

    @Override
    public boolean isFocused() {
        return this.parent.getPrompt() == this;
    }

    private void close() {
        this.parent.setPrompt(null);
    }

    private void runAction() {
        this.action.runnable.run();
        this.close();
    }

    public record Action(Text label, Runnable runnable) {

    }
}
