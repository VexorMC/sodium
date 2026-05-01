package net.caffeinemc.mods.sodium.client.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import net.caffeinemc.mods.sodium.client.util.ScissorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class ScrollableTooltip extends DrawableHelper {
    private static final Identifier ARROW_TEXTURE = new Identifier("radium", "textures/gui/tooltip_arrows.png");
    private static final int ARROW_WIDTH = 5;
    private static final int SPRITE_WIDTH = 10;
    private static final int ARROW_HEIGHT = 9;

    private static final int TEXT_HORIZONTAL_PADDING = Layout.INNER_MARGIN - 1;
    private static final int TEXT_VERTICAL_PADDING = TEXT_HORIZONTAL_PADDING;

    private final TextRenderer font = MinecraftClient.getInstance().textRenderer;
    private ControlElement hoveredElement;
    private ScrollbarWidget scrollbar;
    private final Vector2i contentSize = new Vector2i();
    private Dim2i visibleDim;
    private boolean overlayMode;
    private final List<String> content = new ArrayList<>();
    private final TooltipParent parent;
    private Dim2i tooltipArea; // area for the tooltip to be within
    private final Vector2i reservedArea = new Vector2i(); // area reserved for action buttons

    public ScrollableTooltip(TooltipParent parent) {
        this.parent = parent;
    }

    public interface TooltipParent {
        <T extends GuiEventListener & Renderable> T addRenderableWidget(T guiEventListener);
        void removeWidget(GuiEventListener guiEventListener);
    }

    public void setTooltipArea(Dim2i area) {
        this.tooltipArea = area;
    }

    public void onControlHover(ControlElement hovered, int mouseX, int mouseY) {
        if (hovered != null) {
            this.hoveredElement = hovered;

            if (this.scrollbar != null) {
                this.parent.removeWidget(this.scrollbar);
                this.scrollbar = null;
            }

            // re-flow the content if we need a scrollbar, which takes up some width
            if (this.positionTooltip(false)) {
                this.positionTooltip(true);

                this.scrollbar = this.parent.addRenderableWidget(new ScrollbarWidget(new Dim2i(
                        this.visibleDim.getLimitX() - Layout.SCROLLBAR_WIDTH,
                        this.visibleDim.y(),
                        Layout.SCROLLBAR_WIDTH,
                        this.visibleDim.height()
                ), false, true));
                this.scrollbar.setScrollbarContext(this.visibleDim.height(), this.contentSize.y());
            }
        } else if (this.hoveredElement != null) {
            this.positionTooltip(this.scrollbar != null);

            // handle the space between options and their tooltip
            if ((mouseX < this.hoveredElement.getLimitX() || mouseX >= this.visibleDim.x() ||
                    mouseY < this.hoveredElement.getY() || mouseY >= this.hoveredElement.getLimitY()) &&
                    !this.visibleDim.containsCursor(mouseX, mouseY)) {
                this.hoveredElement = null;

                if (this.scrollbar != null) {
                    this.parent.removeWidget(this.scrollbar);
                    this.scrollbar = null;
                }
            }
        }
    }

    private int getLineHeight() {
        return this.font.fontHeight + Layout.TEXT_LINE_SPACING;
    }

    private int generateTooltipContent(int boxWidth, boolean needsScrolling) {
        int textWidth = boxWidth - TEXT_HORIZONTAL_PADDING * 2;
        if (needsScrolling) {
            textWidth -= Layout.SCROLLBAR_WIDTH;
        }

        var option = this.hoveredElement.getOption();

        this.content.clear();
        this.content.addAll(this.font.wrapLines(option.getTooltip().asFormattedString(), textWidth));

        OptionImpact impact = option.getImpact();
        if (impact != null) {
            var impactText = new TranslatableText("sodium.options.performance_impact_string", impact.getName());
            this.content.addAll(this.font.wrapLines(impactText.setStyle(new Style().setFormatting(Formatting.GRAY)).asFormattedString(), textWidth));
        }

        return this.content.size() * this.getLineHeight() - Layout.TEXT_LINE_SPACING + TEXT_VERTICAL_PADDING * 2;
    }

    private boolean positionTooltip(boolean needsScrolling) {
        int defaultBoxWidth = Math.min(this.tooltipArea.getLimitX() - this.tooltipArea.x(), Layout.MAX_TOOLTIP_WIDTH);
        int defaultBoxY = this.hoveredElement.getY();
        int defaultBoxX = this.tooltipArea.x();

        int boxWidth = 0, boxX = 0, boxY = 0;
        boolean fixedBoxY = false;
        int boxYCutoff = this.tooltipArea.getLimitY();

        this.overlayMode = defaultBoxWidth < Layout.MIN_TOOLTIP_WIDTH;

        if (!this.overlayMode) {
            // Hovered element above the area so tooltip has full width, needs to be up high enough to not intersect with the area
            if (this.hoveredElement.getLimitY() < this.reservedArea.y) {
                boxWidth = defaultBoxWidth;
                boxX = defaultBoxX;
                boxY = defaultBoxY;

                // set the y cutoff to make sure the bottom of the tooltip is moved up above the reserved area
                boxYCutoff = this.reservedArea.y;
            }

            // Hovered element left of the area but enough space to show tooltip with reduced width
            else if (this.tooltipArea.x() < this.reservedArea.x) {
                int availableWidth = this.reservedArea.x - this.tooltipArea.x();

                if (availableWidth >= Layout.MIN_TOOLTIP_WIDTH) {
                    boxWidth = Math.min(availableWidth, Layout.MAX_TOOLTIP_WIDTH);
                    boxX = defaultBoxX;
                    boxY = defaultBoxY;
                } else {
                    this.overlayMode = true;
                }
            }

            // default non-overlay positioning
            else {
                boxWidth = defaultBoxWidth;
                boxX = defaultBoxX;
                boxY = defaultBoxY;
            }
        }

        if (this.overlayMode) {
            // in overlay mode the tooltip is shown on top of the options list, either above or below
            boxWidth = this.hoveredElement.getWidth() - 2 * Layout.TOOLTIP_OUTER_MARGIN;
            boxX = this.hoveredElement.getX() + Layout.TOOLTIP_OUTER_MARGIN;

            // place the content above or below the hovered element depending on which side has more space
            int spaceAbove = this.hoveredElement.getY() - this.tooltipArea.y();
            int spaceBelow = this.tooltipArea.getLimitY() - this.hoveredElement.getLimitY() - Layout.TOOLTIP_OUTER_MARGIN;
            if (spaceBelow >= spaceAbove) {
                boxY = this.hoveredElement.getLimitY() + Layout.TOOLTIP_OUTER_MARGIN;
                boxYCutoff = this.tooltipArea.getLimitY() - Layout.TOOLTIP_OUTER_MARGIN;
                
                // fix the box's upper y position since moving it up would cause it to overlap the hovered element
                fixedBoxY = true;
            } else {
                boxY = this.hoveredElement.getY() - Layout.TOOLTIP_OUTER_MARGIN;
                boxYCutoff = this.hoveredElement.getY() - Layout.TOOLTIP_OUTER_MARGIN;
                // it automatically gets moved up as far as necessary later
            }
        }

        int contentHeight = this.generateTooltipContent(boxWidth, needsScrolling);
        int boxYLimit = boxY + contentHeight;

        if (!fixedBoxY) {
            // If the box is going to be cut off on the Y-axis, move it back up the difference
            if (boxYLimit > boxYCutoff) {
                boxY -= boxYLimit - boxYCutoff;
            }

            // prevent it from moving up further than the tooltip safe area
            if (boxY < this.tooltipArea.y()) {
                boxY = this.tooltipArea.y();
            }
        }

        this.contentSize.set(boxWidth, contentHeight);

        int maxVisibleHeight = boxYCutoff - boxY;
        int visibleHeight = Math.min(contentHeight, maxVisibleHeight);
        this.visibleDim = new Dim2i(boxX, boxY, boxWidth, visibleHeight);

        return contentHeight > maxVisibleHeight;
    }

    public void render() {
        if (this.hoveredElement == null) {
            return;
        }

        if (!this.overlayMode) {
            // draw small triangular arrow attached to the side of the tooltip box pointing at the hovered element, in the margin between the hovered element and the tooltip box
            int arrowX = this.visibleDim.x() - ARROW_WIDTH;
            int arrowY = this.hoveredElement.getCenterY() - (ARROW_HEIGHT / 2);

            // constraint the arrow to be within the tooltip area
            arrowY = Math.max(arrowY, this.tooltipArea.y());
            arrowY = Math.min(arrowY + ARROW_HEIGHT, this.tooltipArea.getLimitY()) - ARROW_HEIGHT;

            // parameters are: render type, sprite, x, y, u offset, v offset, render width, render height, u size, v size, color

            MinecraftClient.getInstance().getTextureManager().bindTexture(ARROW_TEXTURE);
            GlStateManager.color((Colors.BACKGROUND_LIGHT >> 16 & 0xFF) / 255f, (Colors.BACKGROUND_LIGHT >> 8 & 0xFF) / 255f, (Colors.BACKGROUND_LIGHT & 0xFF) / 255f, 1f);
            DrawableHelper.drawTexture(arrowX, arrowY, ARROW_WIDTH, 0, ARROW_WIDTH, ARROW_HEIGHT, SPRITE_WIDTH, ARROW_HEIGHT);
            GlStateManager.color((Colors.BACKGROUND_DEFAULT >> 16 & 0xFF) / 255f, (Colors.BACKGROUND_DEFAULT >> 8 & 0xFF) / 255f, (Colors.BACKGROUND_DEFAULT & 0xFF) / 255f, 1f);
            DrawableHelper.drawTexture(arrowX, arrowY, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, SPRITE_WIDTH, ARROW_HEIGHT);
        }

        ScissorUtil.withScissor(this.visibleDim.x(), this.visibleDim.y(), this.visibleDim.width(), this.visibleDim.height(), () -> {
            int lineHeight = this.getLineHeight();

            int scrollAmount = 0;
            if (this.scrollbar != null) {
                scrollAmount = this.scrollbar.getScrollAmount();
            }

            var backgroundColor = this.overlayMode ? Colors.BACKGROUND_OVERLAY : Colors.BACKGROUND_LIGHT;

            DrawableHelper.fill(this.visibleDim.x(), this.visibleDim.y(), this.visibleDim.getLimitX(), this.visibleDim.getLimitY(), backgroundColor);
            for (int i = 0; i < this.content.size(); i++) {
                this.font.drawWithShadow(this.content.get(i),
                        this.visibleDim.x() + TEXT_HORIZONTAL_PADDING, this.visibleDim.y() + TEXT_VERTICAL_PADDING + (i * lineHeight) - scrollAmount,
                        Colors.FOREGROUND);
            }
        });
    }

    public boolean mouseScrolled(double d, double e, double amount) {
        if (this.visibleDim != null && this.visibleDim.containsCursor(d, e) && this.scrollbar != null) {
            this.scrollbar.scroll((int) (-amount * 10));
            return true;
        }
        return false;
    }

    /**
     * Sets the position of the top left corner of the reserved area that extends to the right and to the bottom to the screen edges.
     *
     * @param x The x coordinate of the top left corner of the reserved area
     * @param y The y coordinate of the top left corner of the reserved area
     */
    public void setReservedAreaTopLeftCorner(int x, int y) {
        this.reservedArea.set(x - Layout.TOOLTIP_OUTER_MARGIN, y - Layout.TOOLTIP_OUTER_MARGIN);
    }
}
