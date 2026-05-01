package net.caffeinemc.mods.sodium.client.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

/**
 * Right-aligned reset overlay shown when the parent row is hovered while SHIFT is held.
 * It derives is positioning from the parent so it tracks the parent's scroll position automatically.
 */
public class ResetButton extends AbstractWidget {
    private static final Identifier ICON = new Identifier("radium", "textures/gui/reset_button.png");
    private static final int ICON_SIZE = 10;
    private static final int COLOR = 0xFFFF8C30;

    private final AbstractWidget parent;
    private final Runnable action;

    public ResetButton(AbstractWidget parent, Runnable action) {
        super(new Dim2i(0, 0, Layout.BUTTON_SHORT, 0));
        this.parent = parent;
        this.action = action;
    }

    public static boolean isShiftHeld() {
        return Screen.hasShiftDown();
    }

    public boolean isActive() {
        return this.parent.isHovered() && isShiftHeld();
    }

    @Override
    public int getX() {
        return this.parent.getLimitX() - this.getWidth();
    }

    @Override
    public int getY() {
        return this.parent.getY();
    }

    @Override
    public int getHeight() {
        return this.parent.getHeight();
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (!this.isActive()) {
            return;
        }

        int x = this.getCenterX() - ICON_SIZE / 2;
        int y = this.getCenterY() - ICON_SIZE / 2;

        float a = ((COLOR >> 24) & 0xFF) / 255.0f;
        float r = ((COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((COLOR >> 8)  & 0xFF) / 255.0f;
        float b = ( COLOR        & 0xFF) / 255.0f;

        MinecraftClient.getInstance().getTextureManager().bindTexture(ICON);
        GlStateManager.color(r, g, b, a);
        DrawableHelper.drawTexture(x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        GlStateManager.color(1f,  1f, 1f, 1f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isShiftHeld() || button != 0) {
            return false;
        }

        if (!this.parent.isMouseOver(mouseX, mouseY) || !this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        this.action.run();
        this.playClickSound();
        return true;
    }
}