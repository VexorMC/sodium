package net.caffeinemc.mods.sodium.client.gui.widgets;

import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractParentWidget extends AbstractWidget {
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener element : this.children) {
            if (element.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(element);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiEventListener element : this.children) {
            if (element.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        for (GuiEventListener element : this.children) {
            if (element.mouseDragged(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
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
}
