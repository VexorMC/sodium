package net.caffeinemc.mods.sodium.client.gui.widgets;

import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractParentWidget extends AbstractWidget implements GuiEventListener {
    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<Renderable> renderableChildren = new ArrayList<>();

    private GuiEventListener focusedElement;
    private boolean dragging;

    protected AbstractParentWidget(Dim2i dim) {
        super(dim);
    }

    protected <T extends GuiEventListener> T addChild(T element) {
        this.children.add(element);
        return element;
    }

    protected <T extends GuiEventListener & Renderable> T addRenderableChild(T element) {
        this.children.add(element);
        this.renderableChildren.add(element);
        return element;
    }

    protected void removeChild(GuiEventListener element) {
        this.children.remove(element);
        this.renderableChildren.remove(element);
    }

    protected void clearChildren() {
        this.children.clear();
        this.renderableChildren.clear();
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        for (Renderable element : this.renderableChildren) {
            element.render(mouseX, mouseY, delta);
        }
    }

    @Nullable
    public GuiEventListener getFocused() {
        return this.focusedElement;
    }

    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        if (this.focusedElement != null) {
            this.focusedElement.setFocused(false);
        }

        if (guiEventListener != null) {
            guiEventListener.setFocused(true);
        }

        this.focusedElement = guiEventListener;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = this.children.size() - 1; i >= 0; i--) {
            GuiEventListener child = this.children.get(i);
            if (child.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(child);
                if (button == 0) {
                    this.dragging = true;
                }
                return true;
            }
        }

        if (button == 0) {
            this.setFocused((GuiEventListener) null);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.dragging = false;
        }

        boolean handled = false;
        if (this.focusedElement != null) {
            handled = this.focusedElement.mouseReleased(mouseX, mouseY, button);
        }

        for (int i = this.children.size() - 1; i >= 0; i--) {
            GuiEventListener child = this.children.get(i);
            if (child == this.focusedElement) {
                continue;
            }
            if (child.mouseReleased(mouseX, mouseY, button)) {
                handled = true;
                break;
            }
        }

        return handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!this.dragging || button != 0) {
            return false;
        }

        if (this.focusedElement != null && this.focusedElement.mouseDragged(mouseX, mouseY, button)) {
            return true;
        }

        for (int i = this.children.size() - 1; i >= 0; i--) {
            GuiEventListener child = this.children.get(i);
            if (child.mouseDragged(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (int i = this.children.size() - 1; i >= 0; i--) {
            GuiEventListener child = this.children.get(i);
            if (child.isMouseOver(mouseX, mouseY) && child.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }

        return this.focusedElement != null
                && this.focusedElement.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int code, char character) {
        if (this.focusedElement != null && this.focusedElement.keyPressed(code, character)) {
            return true;
        }

        for (int i = this.children.size() - 1; i >= 0; i--) {
            GuiEventListener child = this.children.get(i);
            if (child == this.focusedElement) {
                continue;
            }
            if (child.keyPressed(code, character)) {
                return true;
            }
        }

        return false;
    }

}
