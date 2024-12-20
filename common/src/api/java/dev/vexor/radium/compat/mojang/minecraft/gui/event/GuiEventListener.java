package dev.vexor.radium.compat.mojang.minecraft.gui.event;

public interface GuiEventListener {
    public static final long DOUBLE_CLICK_THRESHOLD_MS = 250L;

    default public boolean mouseClicked(double d, double d2, int n) {
        return false;
    }

    default public boolean mouseReleased(double d, double d2, int n) {
        return false;
    }

    default public boolean mouseDragged(double d, double d2, int n) {
        return false;
    }

    default public boolean keyPressed(int n, int n2, int n3) {
        return false;
    }

    default public boolean isMouseOver(double d, double d2) {
        return false;
    }

    public void setFocused(boolean var1);

    public boolean isFocused();
}