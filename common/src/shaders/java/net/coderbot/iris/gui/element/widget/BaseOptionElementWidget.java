package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public abstract class BaseOptionElementWidget<T extends OptionMenuElement> extends CommentedElementWidget<T> {
	protected static final Text SET_TO_DEFAULT = new TranslatableText("options.iris.setToDefault").setStyle(new Style().setFormatting(Formatting.GREEN));
	protected static final Text DIVIDER = new LiteralText(": ");

	protected Text unmodifiedLabel;
	protected ShaderPackScreen screen;
	protected NavigationController navigation;
	private Text label;

	protected Text trimmedLabel;
	protected Text valueLabel;

	private boolean isLabelTrimmed;
	private int maxLabelWidth;
	private int valueSectionWidth;

	public BaseOptionElementWidget(T element) {
		super(element);
	}

	@Override
	public void init(ShaderPackScreen screen, NavigationController navigation) {
		this.screen = screen;
		this.navigation = navigation;
		this.valueLabel = null;
		this.trimmedLabel = null;
	}

	protected final void setLabel(Text label) {
		this.label = label.copy().append(DIVIDER);
		this.unmodifiedLabel = label;
	}

	protected final void updateRenderParams(int width, int minValueSectionWidth) {
		// Lazy init of value label
		if (this.valueLabel == null) {
			this.valueLabel = createValueLabel();
		}

		// Determine the width of the value box
		TextRenderer font = MinecraftClient.getInstance().textRenderer;
		this.valueSectionWidth = Math.max(minValueSectionWidth, font.getStringWidth(this.valueLabel.asFormattedString()) + 8);

		// Determine maximum width of trimmed label
		this.maxLabelWidth = (width - 8) - this.valueSectionWidth;

		// Lazy init of trimmed label, and make sure it is only trimmed when necessary
		if (this.trimmedLabel == null || font.getStringWidth(this.label.asFormattedString()) > this.maxLabelWidth != isLabelTrimmed) {
			updateLabels();
		}

		// Set whether the label has been trimmed (used when updating label and determining whether to render tooltips)
		this.isLabelTrimmed = font.getStringWidth(this.label.asFormattedString()) > this.maxLabelWidth;
	}

	protected final void renderOptionWithValue(int x, int y, int width, int height, boolean hovered, float sliderPosition, int sliderWidth) {
		GuiUtil.bindIrisWidgetsTexture();

		// Draw button background
		GuiUtil.drawButton(x, y, width, height, hovered, false);

		// Draw the value box
		GuiUtil.drawButton((x + width) - (this.valueSectionWidth + 2), y + 2, this.valueSectionWidth, height - 4, false, true);

		// Draw the preview slider
		if (sliderPosition >= 0) {
			// Range of x values the slider can occupy
			int sliderSpace = (this.valueSectionWidth - 4) - sliderWidth;

			// Position of slider
			int sliderPos = ((x + width) - this.valueSectionWidth) + (int)(sliderPosition * sliderSpace);

			GuiUtil.drawButton(sliderPos, y + 4, sliderWidth, height - 8, false, false);
		}

        TextRenderer font = MinecraftClient.getInstance().textRenderer;

		// Draw the label
		font.drawWithShadow(this.trimmedLabel.asFormattedString(), x + 6, y + 7, 0xFFFFFF);
		// Draw the value label
		font.drawWithShadow(this.valueLabel.asFormattedString(), (x + (width - 2)) - (int)(this.valueSectionWidth * 0.5) - (int)(font.getStringWidth(this.valueLabel.asFormattedString()) * 0.5), y + 7, 0xFFFFFF);
	}

	protected final void renderOptionWithValue(int x, int y, int width, int height, boolean hovered) {
		this.renderOptionWithValue(x, y, width, height, hovered, -1, 0);
	}

	protected final void tryRenderTooltip(int mouseX, int mouseY, boolean hovered) {
		if (Screen.hasShiftDown()) {
			renderTooltip(SET_TO_DEFAULT, mouseX, mouseY, hovered);
		} else if (this.isLabelTrimmed && !this.screen.isDisplayingComment()) {
			renderTooltip(this.unmodifiedLabel, mouseX, mouseY, hovered);
		}
	}

	protected final void renderTooltip(Text text, int mouseX, int mouseY, boolean hovered) {
		if (hovered) {
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(MinecraftClient.getInstance().textRenderer, text, mouseX + 2, mouseY - 16));
		}
	}

	protected final void updateLabels() {
		this.trimmedLabel = createTrimmedLabel();
		this.valueLabel = createValueLabel();
	}

	protected final Text createTrimmedLabel() {
		Text label = GuiUtil.shortenText(
				MinecraftClient.getInstance().textRenderer,
				this.label.copy(),
				this.maxLabelWidth);

		if (this.isValueModified()) {
			label = label.setStyle(new Style().setFormatting(Formatting.YELLOW));
		}

		return label;
	}

	protected abstract Text createValueLabel();

	public abstract boolean applyNextValue();

	public abstract boolean applyPreviousValue();

	public abstract boolean applyOriginalValue();

	public abstract boolean isValueModified();

	public abstract @Nullable String getCommentKey();

	@Override
	public Optional<Text> getCommentTitle() {
		return Optional.of(this.unmodifiedLabel);
	}

	@Override
	public Optional<Text> getCommentBody() {
		return Optional.ofNullable(getCommentKey()).map(key -> !I18n.translate(key).equals(key) ? new TranslatableText(key) : null);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2) {
			boolean refresh = false;

			if (Screen.hasShiftDown()) {
				refresh = applyOriginalValue();
			}
			if (!refresh) {
				if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
					refresh = applyNextValue();
				} else {
					refresh = applyPreviousValue();
				}
			}

			if (refresh) {
				this.navigation.refresh();
			}

			GuiUtil.playButtonClickSound();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}
}
