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

    default public boolean keyPressed(int code, char character) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { return false; }

    default public boolean isMouseOver(double d, double d2) {
        return false;
    }

    default boolean changeFocus(boolean lookForwards) {
        return false;
    }

    public void setFocused(boolean var1);

    public boolean isFocused();
}