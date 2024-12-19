package net.caffeinemc.mods.sodium.client.gui.options.control;

import dev.lunasa.compat.mojang.minecraft.gui.draw.Rect2i;
import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;
import org.lwjgl.input.Keyboard;

public class SliderControl implements Control<Integer> {
    private final Option<Integer> option;

    private final int min, max, interval;

    private final ControlValueFormatter mode;

    public SliderControl(Option<Integer> option, int min, int max, int interval, ControlValueFormatter mode) {
        Validate.isTrue(max > min, "The maximum value must be greater than the minimum value");
        Validate.isTrue(interval > 0, "The slider interval must be greater than zero");
        Validate.isTrue(((max - min) % interval) == 0, "The maximum value must be divisable by the interval");
        Validate.notNull(mode, "The slider mode must not be null");

        this.option = option;
        this.min = min;
        this.max = max;
        this.interval = interval;
        this.mode = mode;
    }

    @Override
    public ControlElement<Integer> createElement(Dim2i dim) {
        return new Button(this.option, dim, this.min, this.max, this.interval, this.mode);
    }

    @Override
    public Option<Integer> getOption() {
        return this.option;
    }

    @Override
    public int getMaxWidth() {
        return 170;
    }

    private static class Button extends ControlElement<Integer> {
        private static final int THUMB_WIDTH = 2, TRACK_HEIGHT = 1;

        private final Rect2i sliderBounds;
        private int contentWidth;
        private final ControlValueFormatter formatter;

        private final int min;
        private final int max;
        private final int range;
        private final int interval;

        private double thumbPosition;

        private boolean sliderHeld;

        public Button(Option<Integer> option, Dim2i dim, int min, int max, int interval, ControlValueFormatter formatter) {
            super(option, dim);

            this.min = min;
            this.max = max;
            this.range = max - min;
            this.interval = interval;
            this.thumbPosition = this.getThumbPositionForValue(option.getValue());
            this.formatter = formatter;

            this.sliderBounds = new Rect2i(dim.getLimitX() - 96, dim.getCenterY() - 5, 90, 10);
            this.sliderHeld = false;
        }

        @Override
        public void render(int mouseX, int mouseY, float delta) {
            int sliderX = this.sliderBounds.getX();
            int sliderY = this.sliderBounds.getY();
            int sliderWidth = this.sliderBounds.getWidth();
            int sliderHeight = this.sliderBounds.getHeight();

            var label = this.formatter.format(this.option.getValue())
                    .copy();

            if (!this.option.isAvailable()) {
                label.setStyle(new Style()
                        .setFormatting(Formatting.GRAY)
                        .setItalic(true));
            }

            int labelWidth = this.font.getStringWidth(label.asFormattedString());

            boolean drawSlider = this.option.isAvailable() && (this.hovered || this.isFocused());
            if (drawSlider) {
                this.contentWidth = sliderWidth + labelWidth;
            } else {
                this.contentWidth = labelWidth;
            }

            // render the label first and then the slider to prevent the highlight rect from darkening the slider
            super.render(mouseX, mouseY, delta);

            if (drawSlider) {
                this.thumbPosition = this.getThumbPositionForValue(this.option.getValue());

                double thumbOffset = MathHelper.clamp((double) (this.getIntValue() - this.min) / this.range * sliderWidth, 0, sliderWidth);

                int thumbX = (int) (sliderX + thumbOffset - THUMB_WIDTH);
                int trackY = (int) (sliderY + (sliderHeight / 2f) - ((double) TRACK_HEIGHT / 2));

                this.drawRect(thumbX, sliderY, thumbX + (THUMB_WIDTH * 2), sliderY + sliderHeight, 0xFFFFFFFF);
                this.drawRect(sliderX, trackY, sliderX + sliderWidth, trackY + TRACK_HEIGHT, 0xFFFFFFFF);

                this.drawString(label, sliderX - labelWidth - 6, sliderY + (sliderHeight / 2) - 4, 0xFFFFFFFF);
            } else {
                this.drawString(label, sliderX + sliderWidth - labelWidth, sliderY + (sliderHeight / 2) - 4, 0xFFFFFFFF);
            }
        }

        @Override
        public int getContentWidth() {
            return this.contentWidth;
        }

        public int getIntValue() {
            return this.min + (this.interval * (int) Math.round(this.getSnappedThumbPosition() / this.interval));
        }

        public double getSnappedThumbPosition() {
            return this.thumbPosition / (1.0D / this.range);
        }

        public double getThumbPositionForValue(int value) {
            return (value - this.min) * (1.0D / this.range);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.sliderHeld = false;

            if (this.option.isAvailable() && button == 0 && this.dim.containsCursor(mouseX, mouseY)) {
                if (this.sliderBounds.contains((int) mouseX, (int) mouseY)) {
                    this.setValueFromMouse(mouseX);
                    this.sliderHeld = true;
                }

                return true;
            }

            return false;
        }

        private void setValueFromMouse(double d) {
            this.setValue((d - (double) this.sliderBounds.getX()) / (double) this.sliderBounds.getWidth());
        }

        public void setValue(double d) {
            this.thumbPosition = MathHelper.clamp(d, 0.0D, 1.0D);

            int value = this.getIntValue();

            if (this.option.getValue() != value) {
                this.option.setValue(value);
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!isFocused()) return false;

            if (keyCode == Keyboard.KEY_LEFT) {
                this.option.setValue(MathHelper.clamp(this.option.getValue() - this.interval, this.min, this.max));
                return true;
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                this.option.setValue(MathHelper.clamp(this.option.getValue() + this.interval, this.min, this.max));
                return true;
            }

            return false;
        }

        @Override
        public boolean mouseDragged(double d, double d2, int n) {
            if (this.option.isAvailable() && n == 0) {
                if (this.sliderHeld) {
                    this.setValueFromMouse(d);
                }

                return true;
            }

            return false;
        }
    }

}
