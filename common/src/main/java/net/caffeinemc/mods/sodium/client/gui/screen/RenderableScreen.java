package net.caffeinemc.mods.sodium.client.gui.screen;

import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RenderableScreen extends Screen {
    protected final List<Renderable> widgets = new ArrayList<>();

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        this.renderBackground();

        for (; !this.client.options.touchscreen && Mouse.next(); this.client.currentScreen.handleMouse()) {
            int dWheel = Mouse.getEventDWheel();

            if (dWheel != 0) {
                getEventListeners().forEach(el -> el.mouseScrolled(mouseX, mouseY, dWheel, dWheel));
            }
        }

        widgets.forEach(renderable -> renderable.render(mouseX, mouseY, tickDelta));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        getEventListeners().forEach(el -> el.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        getEventListeners().forEach(el -> el.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    protected void mouseDragged(int mouseX, int mouseY, int button, long mouseLastClicked) {
        super.mouseDragged(mouseX, mouseY, button, mouseLastClicked);
        getEventListeners().forEach(el -> el.mouseDragged(mouseX, mouseY, button));
    }

    @Override
    protected void keyPressed(char id, int code) {
        super.keyPressed(id, code);
        getEventListeners().forEach(el -> el.keyPressed(code, id));
    }

    public void clearWidgets() {
        this.widgets.clear();
    }

    public void addRenderableWidget(Renderable renderable) {
        this.widgets.add(renderable);
    }

    protected List<GuiEventListener> getEventListeners() {
        return widgets.stream().filter(it -> it instanceof GuiEventListener).map(GuiEventListener.class::cast).collect(Collectors.toList());
    }
}
