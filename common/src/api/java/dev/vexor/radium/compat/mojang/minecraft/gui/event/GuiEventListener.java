package dev.vexor.radium.compat.mojang.minecraft.gui.event;

public interface GuiEventListener {
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean keyPressed(int code, char character) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { return false; }

    default boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    default boolean changeFocus(boolean lookForwards) {
        return false;
    }

    void setFocused(boolean lookForwards);

    boolean isFocused();
}