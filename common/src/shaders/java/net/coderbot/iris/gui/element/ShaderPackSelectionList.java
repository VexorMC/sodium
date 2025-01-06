package net.coderbot.iris.gui.element;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.Window;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ShaderPackSelectionList extends IrisObjectSelectionList {
	private static final Text PACK_LIST_LABEL = new TranslatableText("pack.iris.list.label").setStyle(new Style().setItalic(true).setFormatting(Formatting.GRAY));

	private final ShaderPackScreen screen;
	private final TopButtonRowEntry topButtonRow;
	private ShaderPackEntry applied = null;
    private final List<BaseEntry> entries = new ArrayList<>();
    private BaseEntry selected = null;

    public BaseEntry getSelected() {
        return selected;
    }

    public void setSelected(BaseEntry selected) {
        this.selected = selected;
    }

    public ShaderPackSelectionList(ShaderPackScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 20);

		this.screen = screen;
		this.topButtonRow = new TopButtonRowEntry(this, Iris.getIrisConfig().areShadersEnabled());

		refresh();
	}

    @Override
    protected int getEntryCount() {
        return this.entries.size();
    }

    @Override
	public int getRowWidth() {
		return Math.min(308, width - 50);
	}

	public void refresh() {
		this.entries.clear();

		Collection<String> names;

		try {
			names = Iris.getShaderpacksDirectoryManager().enumerate();
		} catch (Throwable e) {
			Iris.logger.error("Error reading files while constructing selection UI", e);

			// Not translating this since it's going to be seen very rarely,
			// We're just trying to get more information on a seemingly untraceable bug:
			// - https://github.com/IrisShaders/Iris/issues/785
			this.addLabelEntries(
					new LiteralText(""),
					new LiteralText("There was an error reading your shaderpacks directory")
							.setStyle(new Style().setFormatting(Formatting.RED).setBold(true)),
					new LiteralText(""),
					new LiteralText("Check your logs for more information."),
					new LiteralText("Please file an issue report including a log file."),
					new LiteralText("If you are able to identify the file causing this, " +
											 "please include it in your report as well."),
					new LiteralText("Note that this might be an issue with folder " +
											 "permissions; ensure those are correct first.")
			);

			return;
		}

		this.entries.add(topButtonRow);

		// Only allow the enable/disable shaders button if the user has
		// added a shader pack. Otherwise, the button will be disabled.
		topButtonRow.allowEnableShadersButton = names.size() > 0;

		int index = 0;

		for (String name : names) {
			index++;
			addPackEntry(index, name);
		}

		this.addLabelEntries(PACK_LIST_LABEL);
	}

	public void addPackEntry(int index, String name) {
		ShaderPackEntry entry = new ShaderPackEntry(index, this, name);

		Iris.getIrisConfig().getShaderPackName().ifPresent(currentPackName -> {
			if (name.equals(currentPackName)) {
				setSelected(entry);
				setApplied(entry);
			}
		});

		this.entries.add(entry);
	}

	public void addLabelEntries(Text ... lines) {
		for (Text text : lines) {
			this.entries.add(new LabelEntry(text));
		}
	}

	public void select(String name) {
		for (int i = 0; i < getEntryCount(); i++) {
			BaseEntry entry = (BaseEntry) getEntry(i);

			if (entry instanceof ShaderPackEntry && ((ShaderPackEntry)entry).packName.equals(name)) {
				setSelected(entry);

				return;
			}
		}
	}

	public void setApplied(ShaderPackEntry entry) {
		this.applied = entry;
	}

	public ShaderPackEntry getApplied() {
		return this.applied;
	}

	public TopButtonRowEntry getTopButtonRow() {
		return topButtonRow;
	}

    @Override
    public Entry getEntry(int index) {
        return this.entries.get(index);
    }

    public static abstract class BaseEntry extends DrawableHelper implements Entry {
		protected BaseEntry() {}
	}

	public static class ShaderPackEntry extends BaseEntry {
		private final String packName;
		private final ShaderPackSelectionList list;
		private final int index;

		public ShaderPackEntry(int index, ShaderPackSelectionList list, String packName) {
			this.packName = packName;
			this.list = list;
			this.index = index;
		}

		public boolean isApplied() {
			return list.getApplied() == this;
		}

		public boolean isSelected() {
			return list.getSelected() == this;
		}

		public String getPackName() {
			return packName;
		}

        @Override
        public void updatePosition(int index, int x, int y) {

        }

        @Override
		public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
			TextRenderer font = MinecraftClient.getInstance().textRenderer;
			int color = 0xFFFFFF;
			String name = packName;

			boolean shadersEnabled = list.getTopButtonRow().shadersEnabled;

			if (font.getStringWidth(name) > this.list.getRowWidth() - 3) {
				name = font.trimToWidth(name, this.list.getRowWidth() - 8) + "...";
			}

			Text text = new LiteralText(name);

			if (shadersEnabled && this.isApplied()) {
				color = 0xFFF263;
			}


			drawCenteredString(font, text.asFormattedString(), (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, color);
		}

        @Override
        public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
            // Only do anything on left-click
            if (button != 0) {
                return false;
            }

            boolean didAnything = false;

            // UX: If shaders are disabled, then clicking a shader in the list will also
            //     enable shaders on apply. Previously, it was not possible to select
            //     a pack when shaders were disabled, but this was a source of confusion
            //     - people did not realize that they needed to enable shaders before
            //     selecting a shader pack.
            if (!list.getTopButtonRow().shadersEnabled) {
                list.getTopButtonRow().setShadersEnabled(true);
                didAnything = true;
            }

            if (!this.isSelected()) {
                this.list.setSelected(this);
                didAnything = true;
            }

            return didAnything;
        }

        @Override
        public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {

        }
    }

	public static class LabelEntry extends BaseEntry {
		private final Text label;

		public LabelEntry(Text label) {
			this.label = label;
		}


        @Override
        public void updatePosition(int index, int x, int y) {

        }

        @Override
        public void render(int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered) {
            drawCenteredString(MinecraftClient.getInstance().textRenderer, label.asFormattedString(), (x + rowWidth / 2) - 2, y + (rowHeight - 11) / 2, 0xC2C2C2);

        }

        @Override
        public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
            return false;
        }

        @Override
        public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {

        }
    }

	public static class TopButtonRowEntry extends BaseEntry {
		private static final Text REFRESH_SHADER_PACKS_LABEL = new TranslatableText("options.iris.refreshShaderPacks").setStyle(new Style().setFormatting(Formatting.DARK_PURPLE));
		private static final Text NONE_PRESENT_LABEL = new TranslatableText("options.iris.shaders.nonePresent").setStyle(new Style().setFormatting(Formatting.GRAY));
		private static final Text SHADERS_DISABLED_LABEL = new TranslatableText("options.iris.shaders.disabled");
		private static final Text SHADERS_ENABLED_LABEL = new TranslatableText("options.iris.shaders.enabled");
		private static final int REFRESH_BUTTON_WIDTH = 18;

		private final ShaderPackSelectionList list;
		private final IrisElementRow buttons = new IrisElementRow();
		private final EnableShadersButtonElement enableDisableButton;
		private final IrisElementRow.Element refreshPacksButton;

		public boolean allowEnableShadersButton = true;
		public boolean shadersEnabled;

		public TopButtonRowEntry(ShaderPackSelectionList list, boolean shadersEnabled) {
			this.list = list;
			this.shadersEnabled = shadersEnabled;
			this.enableDisableButton = new EnableShadersButtonElement(
					getEnableDisableLabel(),
					button -> {
						if (this.allowEnableShadersButton) {
							setShadersEnabled(!this.shadersEnabled);
							GuiUtil.playButtonClickSound();
							return true;
						}

						return false;
					});
			this.refreshPacksButton = new IrisElementRow.IconButtonElement(
					GuiUtil.Icon.REFRESH,
					button -> {
						this.list.refresh();

						GuiUtil.playButtonClickSound();
						return true;
					});
			this.buttons.add(this.enableDisableButton, 0).add(this.refreshPacksButton, REFRESH_BUTTON_WIDTH);
		}

		public void setShadersEnabled(boolean shadersEnabled) {
			this.shadersEnabled = shadersEnabled;
			this.enableDisableButton.text = getEnableDisableLabel();
			this.list.screen.refreshScreenSwitchButton();
		}

        @Override
        public void updatePosition(int index, int x, int y) {

        }

        @Override
        public void render(int index, int x, int y, int entryWidth, int rowHeight, int mouseX, int mouseY, boolean hovered) {
            this.buttons.setWidth(this.enableDisableButton, (entryWidth - 1) - REFRESH_BUTTON_WIDTH);
            this.enableDisableButton.centerX = x + (int)(entryWidth * 0.5);

            this.buttons.render(x - 2, y - 3, 18, mouseX, mouseY, ((MinecraftClientAccessor)MinecraftClient.getInstance()).getTicker().tickDelta, hovered);

            if (this.refreshPacksButton.isHovered()) {
                ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() ->
                        GuiUtil.drawTextPanel(MinecraftClient.getInstance().textRenderer, REFRESH_SHADER_PACKS_LABEL,
                                (mouseX - 8) - MinecraftClient.getInstance().textRenderer.getStringWidth(REFRESH_SHADER_PACKS_LABEL.asFormattedString()), mouseY - 16));
            }
        }

		private Text getEnableDisableLabel() {
			return this.allowEnableShadersButton ? this.shadersEnabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL : NONE_PRESENT_LABEL;
		}

        @Override
        public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
            return this.buttons.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {

        }

        // Renders the label at an offset as to not look misaligned with the rest of the menu
		public static class EnableShadersButtonElement extends IrisElementRow.TextButtonElement {
			private int centerX;

			public EnableShadersButtonElement(Text text, Function<IrisElementRow.TextButtonElement, Boolean> onClick) {
				super(text, onClick);
			}

			@Override
			public void renderLabel(int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
				int textX = this.centerX - (int)(this.font.getStringWidth(this.text.asFormattedString()) * 0.5);
				int textY = y + (int)((height - 8) * 0.5);

				this.font.drawWithShadow(this.text.asFormattedString(), textX, textY, 0xFFFFFF);
			}
		}
	}
}
