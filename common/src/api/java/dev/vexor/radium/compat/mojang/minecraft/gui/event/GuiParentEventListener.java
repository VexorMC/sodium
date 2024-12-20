package dev.vexor.radium.compat.mojang.minecraft.gui.event;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface GuiParentEventListener extends GuiEventListener {
    List<? extends GuiEventListener> children();

    default Optional<GuiEventListener> hoveredElement(double mouseX, double mouseY) {
        for(GuiEventListener element : this.children()) {
            if (element.isMouseOver(mouseX, mouseY)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(GuiEventListener element : this.children()) {
            if (element.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(element);
                if (button == 0) {
                    this.setDragging(true);
                }
                return true;
            }
        }
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);
        return this.hoveredElement(mouseX, mouseY).filter((element) -> element.mouseReleased(mouseX, mouseY, button)).isPresent();
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button) {
        return this.getFocused() != null && this.isDragging() && button == 0 && this.getFocused().mouseDragged(mouseX, mouseY, button);
    }

    boolean isDragging();

    void setDragging(boolean dragging);

    default boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount, double amount) {
        return this.hoveredElement(mouseX, mouseY).filter((element) -> element.mouseScrolled(mouseX, mouseY, verticalAmount, amount)).isPresent();
    }

    default boolean keyPressed(int keyCode, char typedChar) {
        return this.getFocused() != null && this.getFocused().keyPressed(keyCode, typedChar);
    }

    @Nullable
    GuiEventListener getFocused();

    void setFocused(@Nullable GuiEventListener focused);

    default boolean changeFocus(boolean lookForwards) {
        final GuiEventListener element = this.getFocused();
        if (element == null || !element.changeFocus(lookForwards)) {
            final List<? extends GuiEventListener> list = this.children();
            final int i = list.indexOf(element);
            final int j;
            if (element != null && i >= 0) {
                j = i + (lookForwards ? 1 : 0);
            } else if (lookForwards) {
                j = 0;
            } else {
                j = list.size();
            }


            final ListIterator<? extends GuiEventListener> listIterator = list.listIterator(j);
            final BooleanSupplier booleanSupplier = lookForwards ? listIterator::hasNext : listIterator::hasPrevious;
            final Supplier<? extends GuiEventListener> supplier = lookForwards ? listIterator::next : listIterator::previous;

            GuiEventListener element2;
            do {
                if (!booleanSupplier.getAsBoolean()) {
                    this.setFocused(null);
                    return false;
                }

                element2 = supplier.get();
            } while (!element2.changeFocus(lookForwards));

            this.setFocused(element2);
        }
        return true;
    }
}
