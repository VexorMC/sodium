package net.coderbot.iris.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.FileDialogUtil;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.OptionMenuConstructor;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.mixin.MinecraftClientAccessor;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ShaderPackOptionList extends IrisObjectSelectionList {
	private final List<AbstractElementWidget<?>> elementWidgets = new ArrayList<>();
	private final ShaderPackScreen screen;
	private final NavigationController navigation;
	private OptionMenuContainer container;
    private final List<BaseEntry> entries = new ArrayList<>();

	public ShaderPackOptionList(ShaderPackScreen screen, NavigationController navigation, ShaderPack pack, MinecraftClient client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 24);
		this.navigation = navigation;
		this.screen = screen;

		applyShaderPack(pack);
	}

	public void applyShaderPack(ShaderPack pack) {
		this.container = pack.getMenuContainer();
	}

	public void rebuild() {
        this.entries.clear();
        this.scrollAmount = 0;
		OptionMenuConstructor.constructAndApplyToScreen(this.container, this.screen, this, navigation);
	}

	public void refresh() {
		this.elementWidgets.forEach(widget -> widget.init(this.screen, this.navigation));
	}

    @Override
    protected int getEntryCount() {
        return this.entries.size();
    }

    @Override
	public int getRowWidth() {
		return Math.min(400, width - 12);
	}

	public void addHeader(Text text, boolean backButton) {
		this.addEntry(new HeaderEntry(this.screen, this.navigation, text, backButton));
	}

    protected void addEntry(BaseEntry entry) {
        this.entries.add(entry);
    }

	public void addWidgets(int columns, List<AbstractElementWidget<?>> elements) {
		this.elementWidgets.addAll(elements);

		List<AbstractElementWidget<?>> row = new ArrayList<>();
		for (AbstractElementWidget<?> element : elements) {
			row.add(element);

			if (row.size() >= columns) {
				this.addEntry(new ElementRowEntry(screen, this.navigation, row));
				row = new ArrayList<>(); // Clearing the list would affect the row entry created above
			}
		}

		if (row.size() > 0) {
			while (row.size() < columns) {
				row.add(AbstractElementWidget.EMPTY);
			}

			this.addEntry(new ElementRowEntry(screen, this.navigation, row));
		}
	}

	public NavigationController getNavigation() {
		return navigation;
	}

    @Override
    public Entry getEntry(int index) {
        return this.entries.get(index);
    }

    public abstract static class BaseEntry extends DrawableHelper implements Entry {
		protected final NavigationController navigation;

		protected BaseEntry(NavigationController navigation) {
			this.navigation = navigation;
		}
	}


	public static class HeaderEntry extends BaseEntry {
		public static final Text BACK_BUTTON_TEXT = new LiteralText("< ").append(new TranslatableText("options.iris.back").setStyle(new Style().setFormatting(Formatting.ITALIC)));
		public static final Text RESET_BUTTON_TEXT_INACTIVE = new TranslatableText("options.iris.reset").setStyle(new Style().setFormatting(Formatting.GRAY));
            public static final Text RESET_BUTTON_TEXT_ACTIVE = new TranslatableText("options.iris.reset").setStyle(new Style().setFormatting(Formatting.YELLOW));

		public static final Text RESET_HOLD_SHIFT_TOOLTIP = new TranslatableText("options.iris.reset.tooltip.holdShift").setStyle(new Style().setFormatting(Formatting.GOLD));
		public static final Text RESET_TOOLTIP = new TranslatableText("options.iris.reset.tooltip").setStyle(new Style().setFormatting(Formatting.RED));
		public static final Text IMPORT_TOOLTIP = new TranslatableText("options.iris.importSettings.tooltip")
				.setStyle(new Style().setFormatting(Formatting.AQUA));
		public static final Text EXPORT_TOOLTIP = new TranslatableText("options.iris.exportSettings.tooltip")
				.setStyle(new Style().setFormatting(Formatting.DARK_AQUA));

		private static final int MIN_SIDE_BUTTON_WIDTH = 42;
		private static final int BUTTON_HEIGHT = 16;

		private final ShaderPackScreen screen;
		private final @Nullable IrisElementRow backButton;
		private final IrisElementRow utilityButtons = new IrisElementRow();
		private final IrisElementRow.TextButtonElement resetButton;
		private final IrisElementRow.IconButtonElement importButton;
		private final IrisElementRow.IconButtonElement exportButton;
		private final Text text;

		public HeaderEntry(ShaderPackScreen screen, NavigationController navigation, Text text, boolean hasBackButton) {
			super(navigation);

			if (hasBackButton) {
				this.backButton = new IrisElementRow().add(
						new IrisElementRow.TextButtonElement(BACK_BUTTON_TEXT, this::backButtonClicked),
						Math.max(MIN_SIDE_BUTTON_WIDTH, MinecraftClient.getInstance().textRenderer.getStringWidth(BACK_BUTTON_TEXT.asFormattedString()) + 8)
				);
			} else {
				this.backButton = null;
			}

			this.resetButton = new IrisElementRow.TextButtonElement(
					RESET_BUTTON_TEXT_INACTIVE, this::resetButtonClicked);
			this.importButton = new IrisElementRow.IconButtonElement(
					GuiUtil.Icon.IMPORT, GuiUtil.Icon.IMPORT_COLORED, this::importSettingsButtonClicked);
			this.exportButton = new IrisElementRow.IconButtonElement(
					GuiUtil.Icon.EXPORT, GuiUtil.Icon.EXPORT_COLORED, this::exportSettingsButtonClicked);

			this.utilityButtons
					.add(this.importButton, 15)
					.add(this.exportButton, 15)
					.add(this.resetButton, Math.max(MIN_SIDE_BUTTON_WIDTH, MinecraftClient.getInstance().textRenderer.getStringWidth(RESET_BUTTON_TEXT_INACTIVE.asFormattedString()) + 8));

			this.screen = screen;
			this.text = text;
		}


        @Override
        public void updatePosition(int index, int x, int y) {

        }

        @Override
		public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
			// Draw dividing line
			DrawableHelper.fill(x - 3, (y + entryHeight) - 2, x + entryWidth, (y + entryHeight) - 1, 0x66BEBEBE);

			TextRenderer font = MinecraftClient.getInstance().textRenderer;

			// Draw header text
			drawCenteredString(font, text.asFormattedString(), x + (int)(entryWidth * 0.5), y + 5, 0xFFFFFF);

			GuiUtil.bindIrisWidgetsTexture();

            float tickDelta = ((MinecraftClientAccessor)MinecraftClient.getInstance()).getTicker().tickDelta;

			// Draw back button if present
			if (this.backButton != null) {
				backButton.render(x, y, BUTTON_HEIGHT, mouseX, mouseY, tickDelta, hovered);
			}

			boolean shiftDown = Screen.hasShiftDown();

			// Set the appearance of the reset button
			this.resetButton.disabled = !shiftDown;
			this.resetButton.text = shiftDown ? RESET_BUTTON_TEXT_ACTIVE : RESET_BUTTON_TEXT_INACTIVE;

			// Draw the utility buttons
			this.utilityButtons.renderRightAligned((x + entryWidth) - 3, y, BUTTON_HEIGHT, mouseX, mouseY, tickDelta, hovered);

			// Draw the reset button's tooltip
			if (this.resetButton.isHovered()) {
				Text tooltip = shiftDown ? RESET_TOOLTIP : RESET_HOLD_SHIFT_TOOLTIP;
				queueBottomRightAnchoredTooltip(mouseX, mouseY, font, tooltip);
			}
			// Draw the import/export button tooltips
			if (this.importButton.isHovered()) {
				queueBottomRightAnchoredTooltip(mouseX, mouseY, font, IMPORT_TOOLTIP);
			}
			if (this.exportButton.isHovered()) {
				queueBottomRightAnchoredTooltip(mouseX, mouseY, font, EXPORT_TOOLTIP);
			}
		}

		private void queueBottomRightAnchoredTooltip(int x, int y, TextRenderer font, Text text) {
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(
					font, text,
					x - (font.getStringWidth(text.asFormattedString()) + 10), y - 16
			));
		}

        @Override
        public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
            boolean backButtonResult = backButton != null && backButton.mouseClicked(mouseX, mouseY, button);
            boolean utilButtonResult = utilityButtons.mouseClicked(mouseX, mouseY, button);

            return backButtonResult || utilButtonResult;
        }

        @Override
        public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {

        }


        private boolean backButtonClicked(IrisElementRow.TextButtonElement button) {
			this.navigation.back();
			GuiUtil.playButtonClickSound();

			return true;
		}

		private boolean resetButtonClicked(IrisElementRow.TextButtonElement button) {
			if (Screen.hasShiftDown()) {
				Iris.resetShaderPackOptionsOnNextReload();
				this.screen.applyChanges();
				GuiUtil.playButtonClickSound();

				return true;
			}

			return false;
		}

		private boolean importSettingsButtonClicked(IrisElementRow.IconButtonElement button) {
			GuiUtil.playButtonClickSound();

			// Invalid state to be in
			if (!Iris.getCurrentPack().isPresent()) {
				return false;
			}

			// Displaying a dialog when the game is full-screened can cause severe issues
			// https://github.com/IrisShaders/Iris/issues/1258
			if (MinecraftClient.getInstance().isFullscreen()) {
				this.screen.displayNotification(
					new TranslatableText("options.iris.mustDisableFullscreen")
						.setStyle(new Style().setFormatting(Formatting.RED).setBold(true)));
				return false;
			}

			final ShaderPackScreen originalScreen = this.screen; // Also used to prevent invalid state

			FileDialogUtil.fileSelectDialog(
					FileDialogUtil.DialogType.OPEN, "Import Shader Settings from File",
					Iris.getShaderpacksDirectory().resolve(Iris.getCurrentPackName() + ".txt"),
					 "Shader Pack Settings (.txt)", "*.txt")
			.whenComplete((path, err) -> {
				if (err != null) {
					Iris.logger.error("Error selecting shader settings from file", err);

					return;
				}

				if (MinecraftClient.getInstance().currentScreen == originalScreen) {
					path.ifPresent(originalScreen::importPackOptions);
				}
			});

			return true;
		}

		private boolean exportSettingsButtonClicked(IrisElementRow.IconButtonElement button) {
			GuiUtil.playButtonClickSound();

			// Invalid state to be in
			if (!Iris.getCurrentPack().isPresent()) {
				return false;
			}

			// Displaying a dialog when the game is full-screened can cause severe issues
			// https://github.com/IrisShaders/Iris/issues/1258
			if (MinecraftClient.getInstance().isFullscreen()) {
				this.screen.displayNotification(
					new TranslatableText("options.iris.mustDisableFullscreen")
						.setStyle(new Style().setFormatting(Formatting.RED).setBold(true)));
				return false;
			}

			FileDialogUtil.fileSelectDialog(
					FileDialogUtil.DialogType.SAVE, "Export Shader Settings to File",
					Iris.getShaderpacksDirectory().resolve(Iris.getCurrentPackName() + ".txt"),
					"Shader Pack Settings (.txt)", "*.txt")
			.whenComplete((path, err) -> {
				if (err != null) {
					Iris.logger.error("Error selecting file to export shader settings", err);

					return;
				}

				path.ifPresent(p -> {
					Properties toSave = new Properties();

					// Dirty way of getting the currently applied settings as a Properties, directly
					// opens and copies out of the saved settings file if it is present
					Path sourceTxtPath = Iris.getShaderpacksDirectory().resolve(Iris.getCurrentPackName() + ".txt");
					if (Files.exists(sourceTxtPath)) {
						try (InputStream in = Files.newInputStream(sourceTxtPath)) {
							toSave.load(in);
						} catch (IOException ignored) {}
					}

					// Save properties to user determined file
					try (OutputStream out = Files.newOutputStream(p)) {
						toSave.store(out, null);
					} catch (IOException e) {
						Iris.logger.error("Error saving properties to \"" + p + "\"", e);
					}
				});
			});

			return true;
		}
	}

	public static class ElementRowEntry extends BaseEntry {
		private final List<AbstractElementWidget<?>> widgets;
		private final ShaderPackScreen screen;

		private int cachedWidth;
		private int cachedPosX;

		public ElementRowEntry(ShaderPackScreen screen, NavigationController navigation, List<AbstractElementWidget<?>> widgets) {
			super(navigation);

			this.screen = screen;
			this.widgets = widgets;
		}

		@Override
		public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
			this.cachedWidth = entryWidth;
			this.cachedPosX = x;

			// The amount of space widgets will occupy, excluding margins. Will be divided up between widgets.
			int totalWidthWithoutMargins = entryWidth - (2 * (widgets.size() - 1));

			totalWidthWithoutMargins -= 3; // Centers it for some reason

			// Width of a single widget
			float singleWidgetWidth = (float) totalWidthWithoutMargins / widgets.size();

			for (int i = 0; i < widgets.size(); i++) {
				AbstractElementWidget<?> widget = widgets.get(i);
				boolean widgetHovered = hovered && (getHoveredWidget(mouseX) == i);
				widget.render(x + (int)((singleWidgetWidth + 2) * i), y, (int) singleWidgetWidth, entryHeight + 2, mouseX, mouseY, ((MinecraftClientAccessor)MinecraftClient.getInstance()).getTicker().tickDelta, widgetHovered);

				screen.setElementHoveredStatus(widget, widgetHovered);
			}
		}

		public int getHoveredWidget(int mouseX) {
			float positionAcrossWidget = ((float) MathHelper.clamp(mouseX - cachedPosX, 0, cachedWidth)) / cachedWidth;

			return MathHelper.clamp((int) Math.floor(widgets.size() * positionAcrossWidget), 0, widgets.size() - 1);
		}


        @Override
        public void updatePosition(int index, int x, int y) {

        }

        @Override
        public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
            return this.widgets.get(getHoveredWidget((int) mouseX)).mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {
            this.widgets.get(getHoveredWidget((int) mouseX)).mouseReleased(mouseX, mouseY, button);
        }
    }
}
