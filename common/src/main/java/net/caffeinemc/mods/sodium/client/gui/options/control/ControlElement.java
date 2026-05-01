package net.caffeinemc.mods.sodium.client.gui.options.control;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.caffeinemc.mods.sodium.client.config.structure.Option;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;


import java.awt.*;

public abstract class ControlElement extends AbstractWidget {
    protected final AbstractOptionList list;
    protected final ColorTheme theme;

    public ControlElement(AbstractOptionList list, Dim2i dim, ColorTheme theme) {
        super(dim);
        this.list = list;
        this.theme = theme;
    }

    public abstract Option getOption();

    public int getContentWidth() {
        return this.getOption().getControl().getMaxWidth();
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        String name = this.getOption().getName().asFormattedString();

        // add the star suffix before truncation to prevent it from overlapping with the label text
        if (this.getOption().isEnabled() && this.getOption().hasChanged()) {
            name = name + " *";
        }

        name = truncateLabelToFit(name);

        String label;
        if (this.getOption().isEnabled()) {
            if (this.getOption().hasChanged()) {
                label = Formatting.ITALIC + name;
            } else {
                label = Formatting.WHITE + name;
            }
        } else {
            label = String.valueOf(Formatting.GRAY) + Formatting.STRIKETHROUGH + name;
        }

        this.hovered = this.isMouseOver(mouseX, mouseY);

        this.drawRect(this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), this.hovered ? Colors.BACKGROUND_HOVER : Colors.BACKGROUND_LIGHT);
        this.drawString(label, this.getX() + Layout.OPTION_TEXT_SIDE_PADDING, this.getCenterY() + Layout.REGULAR_TEXT_BASELINE_OFFSET, Colors.FOREGROUND);

        if (this.isFocused()) {
            this.drawBorder(this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), -1);
        }
    }

    protected Text formatDisabledControlValue(Text value) {
        return value.copy().setStyle(new Style()
                .setFormatting(Formatting.GRAY)
                .setItalic(true)
        );
    }

    protected String truncateLabelToFit(String name) {
        return truncateTextToFit(name, this.getWidth() - this.getContentWidth() - 20);
    }

    @Override
    public int getY() {
        return super.getY() - this.list.getScrollAmount();
    }

}
