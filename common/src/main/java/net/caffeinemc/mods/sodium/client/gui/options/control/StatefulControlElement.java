package net.caffeinemc.mods.sodium.client.gui.options.control;

import net.caffeinemc.mods.sodium.client.config.structure.StatefulOption;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.widgets.ResetButton;
import net.caffeinemc.mods.sodium.client.util.Dim2i;

public abstract class StatefulControlElement extends ControlElement {
    protected final ResetButton resetButton;

    public StatefulControlElement(AbstractOptionList list, Dim2i dim, ColorTheme theme) {
        super(list, dim, theme);

        this.resetButton = new ResetButton(this, () -> this.getOption().resetToDefault());
    }

    public boolean isResetOverlayActive() {
        return this.resetButton.isActive();
    }

    public abstract StatefulOption<?> getOption();

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.hovered = this.isMouseOver(mouseX, mouseY);

        super.render(mouseX, mouseY, delta);

        this.resetButton.render(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.resetButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected String truncateLabelToFit(String name) {
        int rightReserve = this.isResetOverlayActive() ? this.resetButton.getWidth() : this.getContentWidth() + 20;
        return truncateTextToFit(name, this.getWidth() - rightReserve);
    }
}