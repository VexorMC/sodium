package net.caffeinemc.mods.sodium.client.gui.screen;

import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import gg.sona.radium.mixin.sodium.core.access.AGameRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RenderableScreen extends Screen {
    private static final Identifier BLUR_SHADER_ID = new Identifier("shaders/post/menu_blur.json");
    protected final List<Renderable> widgets = new ArrayList<>();

    @Override
    public void init() {
        ((AGameRenderer)this.client.gameRenderer).invokeLoadShader(BLUR_SHADER_ID);
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        super.render(mouseX, mouseY, tickDelta);

        if (this.client.world == null) {
            this.renderDirtBackground(0);
        }

        if (Mouse.hasWheel()) {
            int dWheel = Mouse.getDWheel();

            if (this.mouseScrolled(mouseX, mouseY, dWheel, 1.0d)) {
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

    protected boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return true;
    }

    @Override
    protected void keyPressed(char id, int code) {
        super.keyPressed(id, code);
        getEventListeners().forEach(el -> el.keyPressed(code, id));
    }

    @Override
    public void removed() {
        this.client.gameRenderer.disableShader();
    }

    public void clearWidgets() {
        this.widgets.clear();
    }

    public <T extends GuiEventListener & Renderable> T addRenderableWidget(T renderable) {
        this.widgets.add(renderable);
        return renderable;
    }

    public void removeWidget(GuiEventListener renderable) {
        this.widgets.remove(renderable);
    }

    protected List<GuiEventListener> getEventListeners() {
        return widgets.stream().filter(it -> it instanceof GuiEventListener).map(GuiEventListener.class::cast).collect(Collectors.toList());
    }
}
