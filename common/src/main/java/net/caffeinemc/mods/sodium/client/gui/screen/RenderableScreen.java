package net.caffeinemc.mods.sodium.client.gui.screen;

import dev.lunasa.compat.mojang.minecraft.gui.Renderable;
import dev.lunasa.compat.mojang.minecraft.gui.event.GuiEventListener;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RenderableScreen extends Screen {
    private final List<Renderable> renderables = new ArrayList<>();

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        super.render(mouseX, mouseY, tickDelta);

        renderables.forEach(renderable -> renderable.render(mouseX, mouseY, tickDelta));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        for (GuiEventListener listener : getEventListeners()) {
            if (listener.mouseClicked(mouseX, mouseY, button)) {
                return;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        for (GuiEventListener listener : getEventListeners()) {
            if (listener.mouseReleased(mouseX, mouseY, button)) {
                return;
            }
        }
    }

    @Override
    protected void mouseDragged(int mouseX, int mouseY, int button, long mouseLastClicked) {
        super.mouseDragged(mouseX, mouseY, button, mouseLastClicked);
        for (GuiEventListener listener : getEventListeners()) {
            if (listener.mouseDragged(mouseX, mouseY, button)) {
                return;
            }
        }
    }

    @Override
    protected void keyPressed(char id, int code) {
        super.keyPressed(id, code);
        for (GuiEventListener listener : getEventListeners()) {
            if (listener.keyPressed(code, 0, 0)) {
                return;
            }
        }
    }

    protected List<GuiEventListener> getEventListeners() {
        return renderables.stream().filter(it -> it instanceof GuiEventListener).map(GuiEventListener.class::cast).collect(Collectors.toList());
    }
}
