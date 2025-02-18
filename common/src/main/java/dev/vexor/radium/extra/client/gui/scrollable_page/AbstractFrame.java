package dev.vexor.radium.extra.client.gui.scrollable_page;

import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFrame extends AbstractWidget implements GuiEventListener {
    protected final Dim2i dim;
    protected final List<AbstractWidget> children = new ArrayList<>();
    protected final List<ControlElement<?>> controlElements = new ArrayList<>();
    private GuiEventListener focused;
    private boolean dragging;

    public AbstractFrame(Dim2i dim) {
        this.dim = dim;
    }

    public void buildFrame() {
        for (GuiEventListener element : this.children) {
            if (element instanceof AbstractFrame abstractFrame) {
                this.controlElements.addAll(abstractFrame.controlElements);
            }
            if (element instanceof ControlElement<?>) {
                this.controlElements.add((ControlElement<?>) element);
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        for (Renderable renderable : this.children) {
            renderable.render(mouseX, mouseY, delta);
        }
    }

    public void applyScissor(int x, int y, int width, int height, Runnable action) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, x + width, y + height);
        action.run();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.dim.containsCursor(mouseX, mouseY);
    }
}