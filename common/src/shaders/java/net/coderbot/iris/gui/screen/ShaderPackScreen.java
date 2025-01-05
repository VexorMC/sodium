package net.coderbot.iris.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.ShaderPackOptionList;
import net.coderbot.iris.gui.element.ShaderPackSelectionList;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.CommentedElementWidget;
import net.coderbot.iris.gui.element.widget.IrisButtonWidget;
import net.coderbot.iris.gui.element.widget.IrisClickableImageWidget;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ShaderPackScreen extends Screen implements HudHideable {
	/**
	 * Queue rendering to happen on top of all elements. Useful for tooltips or dialogs.
	 */
	public static final Set<Runnable> TOP_LAYER_RENDER_QUEUE = new HashSet<>();

	private static final Text SELECT_TITLE = new TranslatableText("pack.iris.select.title").setStyle(new Style().setFormatting(Formatting.GRAY).setItalic(true));
	private static final Text CONFIGURE_TITLE = new TranslatableText("pack.iris.configure.title").setStyle(new Style().setFormatting(Formatting.GRAY).setItalic(true));
	private static final int COMMENT_PANEL_WIDTH = 314;

	private final Screen parent;
	private final Text irisTextComponent;

	private ShaderPackSelectionList shaderPackList;

	private @Nullable ShaderPackOptionList shaderOptionList = null;
	private @Nullable NavigationController navigation = null;
	private ButtonWidget screenSwitchButton;

	private Text notificationDialog = null;
	private int notificationDialogTimer = 0;

	private @Nullable AbstractElementWidget<?> hoveredElement = null;
	private Optional<Text> hoveredElementCommentTitle = Optional.empty();
	private List<String> hoveredElementCommentBody = new ArrayList<>();
	private int hoveredElementCommentTimer = 0;

	private boolean optionMenuOpen = false;

	private boolean dropChanges = false;
	private static String development = "Development Environment";
	private Text developmentComponent;
	private Text updateComponent;

	private boolean guiHidden = false;
	private float guiButtonHoverTimer = 0.0f;

	public ShaderPackScreen(Screen parent) {
		this.parent = parent;

		String irisName = Iris.MODNAME + " " + Iris.getVersion();

		if (irisName.contains("-development-environment")) {
			this.developmentComponent = new LiteralText("Development Environment").setStyle(new Style().setFormatting(Formatting.GOLD));
			irisName = irisName.replace("-development-environment", "");
		}

		this.irisTextComponent = new LiteralText(irisName).setStyle(new Style().setFormatting(Formatting.GRAY));

		if (Iris.getUpdateChecker().getUpdateMessage().isPresent()) {
			this.updateComponent = new LiteralText("New update available!").setStyle(new Style().setFormatting(Formatting.GREEN).setUnderline(true));
			irisTextComponent.append(new LiteralText(" (outdated)").setStyle(new Style().setFormatting(Formatting.RED)));
		}

		refreshForChangedPack();
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		if (this.client.world == null) {
			this.renderBackground();
		} else if (!this.guiHidden) {
			this.fillGradient(0, 0, width, height, 0x4F232323, 0x4F232323);
		}

		if (!this.guiHidden) {
			if (optionMenuOpen && this.shaderOptionList != null) {
				this.shaderOptionList.render(mouseX, mouseY, delta);
			} else {
				this.shaderPackList.render(mouseX, mouseY, delta);
			}
		}

		float previousHoverTimer = this.guiButtonHoverTimer;
		super.render(mouseX, mouseY, delta);
		if (previousHoverTimer == this.guiButtonHoverTimer) {
			this.guiButtonHoverTimer = 0.0f;
		}

		if (!this.guiHidden) {
			drawCenteredString(this.textRenderer, "Iris Options", (int) (this.width * 0.5), 8, 0xFFFFFF);

			if (notificationDialog != null && notificationDialogTimer > 0) {
				drawCenteredString(this.textRenderer, notificationDialog.asFormattedString(), (int) (this.width * 0.5), 21, 0xFFFFFF);
			} else {
				if (optionMenuOpen) {
					drawCenteredString(this.textRenderer, CONFIGURE_TITLE.asFormattedString(), (int) (this.width * 0.5), 21, 0xFFFFFF);
				} else {
					drawCenteredString(this.textRenderer, SELECT_TITLE.asFormattedString(), (int) (this.width * 0.5), 21, 0xFFFFFF);
				}
			}

			// Draw the comment panel
			if (this.isDisplayingComment()) {
				// Determine panel height and position
				int panelHeight = Math.max(50, 18 + (this.hoveredElementCommentBody.size() * 10));
				int x = (int) (0.5 * this.width) - 157;
				int y = this.height - (panelHeight + 4);
				// Draw panel
				GuiUtil.drawPanel(x, y, COMMENT_PANEL_WIDTH, panelHeight);
				// Draw text
				this.textRenderer.drawWithShadow(this.hoveredElementCommentTitle.orElse(new LiteralText("")).asFormattedString(), x + 4, y + 4, 0xFFFFFF);
				for (int i = 0; i < this.hoveredElementCommentBody.size(); i++) {
					this.textRenderer.drawWithShadow(this.hoveredElementCommentBody.get(i), x + 4, (y + 16) + (i * 10), 0xFFFFFF);
				}
			}
		}

		// Render everything queued to render last
		for (Runnable render : TOP_LAYER_RENDER_QUEUE) {
			render.run();
		}
		TOP_LAYER_RENDER_QUEUE.clear();

		if (this.developmentComponent != null) {
			this.textRenderer.drawWithShadow(developmentComponent.asFormattedString(), 2, this.height - 10, 0xFFFFFF);
			this.textRenderer.drawWithShadow(irisTextComponent.asFormattedString(), 2, this.height - 20, 0xFFFFFF);
		} else if (this.updateComponent != null) {
			this.textRenderer.drawWithShadow(updateComponent.asFormattedString(), 2, this.height - 10, 0xFFFFFF);
			this.textRenderer.drawWithShadow(irisTextComponent.asFormattedString(), 2, this.height - 20, 0xFFFFFF);
		} else {
			this.textRenderer.drawWithShadow(irisTextComponent.asFormattedString(), 2, this.height - 10, 0xFFFFFF);
		}
	}

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int widthValue = this.textRenderer.getStringWidth("New update available!");
        if (this.updateComponent != null && mouseX < widthValue && mouseY > (this.height - 10) && mouseY < this.height) {
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

	@Override
	public void init() {
		super.init();
		int bottomCenter = this.width / 2 - 50;
		int topCenter = this.width / 2 - 76;
		boolean inWorld = this.client.world != null;

		this.shaderPackList = new ShaderPackSelectionList(this, this.client, this.width, this.height, 32, this.height - 58, 0, this.width);

		if (Iris.getCurrentPack().isPresent() && this.navigation != null) {
			ShaderPack currentPack = Iris.getCurrentPack().get();

			this.shaderOptionList = new ShaderPackOptionList(this, this.navigation, currentPack, this.client, this.width, this.height, 32, this.height - 58, 0, this.width);
			this.navigation.setActiveOptionList(this.shaderOptionList);

			this.shaderOptionList.rebuild();
		} else {
			optionMenuOpen = false;
			this.shaderOptionList = null;
		}

		if (inWorld) {
			this.shaderPackList.setRenderSelection(false);
			if (shaderOptionList != null) {
				this.shaderOptionList.setRenderSelection(false);
			}
		}

		this.buttons.clear();

		if (!this.guiHidden) {
			this.buttons.add(new IrisButtonWidget(bottomCenter + 104, this.height - 27, 100, 20,
                    I18n.translate("gui.done"), button -> this.client.setScreen(parent)));

			this.buttons.add(new IrisButtonWidget(bottomCenter, this.height - 27, 100, 20,
				I18n.translate("options.iris.apply"), button -> this.applyChanges()));

			this.buttons.add(new IrisButtonWidget(bottomCenter - 104, this.height - 27, 100, 20,
                    I18n.translate("gui.cancel"), button -> this.dropChangesAndClose()));

			this.buttons.add(new IrisButtonWidget(topCenter - 78, this.height - 51, 152, 20,
				I18n.translate("options.iris.openShaderPackFolder"), button -> openShaderPackFolder()));

			this.screenSwitchButton = new IrisButtonWidget(topCenter + 78, this.height - 51, 152, 20,
				I18n.translate("options.iris.shaderPackList"), button -> {
					this.optionMenuOpen = !this.optionMenuOpen;

					// UX: Apply changes before switching screens to avoid unintuitive behavior
					//
					// Not doing this leads to unintuitive behavior, since selecting a pack in the
					// list (but not applying) would open the settings for the previous pack, rather
					// than opening the settings for the selected (but not applied) pack.
					this.applyChanges();

					this.init();
				}
			);

            this.buttons.add(this.screenSwitchButton);

			refreshScreenSwitchButton();
		}

		if (inWorld) {
			Text showOrHide = this.guiHidden
				? new TranslatableText("options.iris.gui.show")
				: new TranslatableText("options.iris.gui.hide");

			float endOfLastButton = this.width / 2.0f + 154.0f;
			float freeSpace = this.width - endOfLastButton;
			int x;
			if (freeSpace > 100.0f) {
				x = this.width - 50;
			} else if (freeSpace < 20.0f) {
				x = this.width - 20;
			} else {
				x = (int) (endOfLastButton + (freeSpace / 2.0f)) - 10;
			}

			this.buttons.add(new IrisClickableImageWidget(
				x, this.height - 39,
				20, 20,
				this.guiHidden ? 20 : 0, 146, 20,
				GuiUtil.IRIS_WIDGETS_TEX,
				button -> {
					this.guiHidden = !this.guiHidden;
					this.init();
				}
			));
		}

		// NB: Don't let comment remain when exiting options screen
		// https://github.com/IrisShaders/Iris/issues/1494
		this.hoveredElement = null;
		this.hoveredElementCommentTimer = 0;
	}

	public void refreshForChangedPack() {
		if (Iris.getCurrentPack().isPresent()) {
			ShaderPack currentPack = Iris.getCurrentPack().get();

			this.navigation = new NavigationController(currentPack.getMenuContainer());

			if (this.shaderOptionList != null) {
				this.shaderOptionList.applyShaderPack(currentPack);
				this.shaderOptionList.rebuild();
			}
		} else {
			this.navigation = null;
		}

		refreshScreenSwitchButton();
	}

	public void refreshScreenSwitchButton() {
		if (this.screenSwitchButton != null) {
			this.screenSwitchButton.message=(
					optionMenuOpen ?
							new TranslatableText("options.iris.shaderPackList")
							: new TranslatableText("options.iris.shaderPackSettings")
			).asFormattedString();
			this.screenSwitchButton.active = optionMenuOpen || shaderPackList.getTopButtonRow().shadersEnabled;
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (this.notificationDialogTimer > 0) {
			this.notificationDialogTimer--;
		}

		if (this.hoveredElement != null) {
			this.hoveredElementCommentTimer++;
		} else {
			this.hoveredElementCommentTimer = 0;
		}
	}

    @Override
    protected void keyPressed(char id, int code) {
        if (code == Keyboard.KEY_ESCAPE) {
            if (this.guiHidden) {
                this.guiHidden = false;
                this.init();

                return;
            } else if (this.navigation != null && this.navigation.hasHistory()) {
                this.navigation.back();

                return;
            } else if (this.optionMenuOpen) {
                this.optionMenuOpen = false;
                this.init();

                return;
            }
        }

        super.keyPressed(id, code);
    }

	public void onPackListFilesDrop(List<Path> paths) {
		List<Path> packs = paths.stream().filter(Iris::isValidShaderpack).collect(Collectors.toList());

		for (Path pack : packs) {
			String fileName = pack.getFileName().toString();

			try {
				Iris.getShaderpacksDirectoryManager().copyPackIntoDirectory(fileName, pack);
			} catch (FileAlreadyExistsException e) {
				this.notificationDialog = new TranslatableText(
						"options.iris.shaderPackSelection.copyErrorAlreadyExists",
						fileName
				).setStyle(new Style().setFormatting(Formatting.RED).setItalic(true));

				this.notificationDialogTimer = 100;
				this.shaderPackList.refresh();

				return;
			} catch (IOException e) {
				Iris.logger.warn("Error copying dragged shader pack", e);

				this.notificationDialog = new TranslatableText(
						"options.iris.shaderPackSelection.copyError",
						fileName
				).setStyle(new Style().setFormatting(Formatting.RED).setItalic(true));

				this.notificationDialogTimer = 100;
				this.shaderPackList.refresh();

				return;
			}
		}

		// After copying the relevant files over to the folder, make sure to refresh the shader pack list.
		this.shaderPackList.refresh();

		if (packs.size() == 0) {
			// If zero packs were added, then notify the user that the files that they added weren't actually shader
			// packs.

			if (paths.size() == 1) {
				// If a single pack could not be added, provide a message with that pack in the file name
				String fileName = paths.get(0).getFileName().toString();

				this.notificationDialog = new TranslatableText(
					"options.iris.shaderPackSelection.failedAddSingle",
					fileName
				).setStyle(new Style().setFormatting(Formatting.RED).setItalic(true));
			} else {
				// Otherwise, show a generic message.

				this.notificationDialog = new TranslatableText(
					"options.iris.shaderPackSelection.failedAdd"
				).setStyle(new Style().setFormatting(Formatting.RED).setItalic(true));
			}

		} else if (packs.size() == 1) {
			// In most cases, users will drag a single pack into the selection menu. So, let's special case it.
			String packName = packs.get(0).getFileName().toString();

			this.notificationDialog = new TranslatableText(
					"options.iris.shaderPackSelection.addedPack",
					packName
			).setStyle(new Style().setFormatting(Formatting.YELLOW).setItalic(true));

			// Select the pack that the user just added, since if a user just dragged a pack in, they'll probably want
			// to actually use that pack afterwards.
			this.shaderPackList.select(packName);
		} else {
			// We also support multiple packs being dragged and dropped at a time. Just show a generic success message
			// in that case.
			this.notificationDialog = new TranslatableText(
					"options.iris.shaderPackSelection.addedPacks",
					packs.size()
			).setStyle(new Style().setFormatting(Formatting.YELLOW).setItalic(true));
		}

		// Show the relevant message for 5 seconds (100 ticks)
		this.notificationDialogTimer = 100;
	}

	public void displayNotification(Text component) {
		this.notificationDialog = component;
		this.notificationDialogTimer = 100;
	}

	public void onOptionMenuFilesDrop(List<Path> paths) {
		// If more than one option file has been dragged, display an error
		// as only one option file should be imported at a time
		if (paths.size() != 1) {
			this.notificationDialog = new TranslatableText(
					"options.iris.shaderPackOptions.tooManyFiles"
			).setStyle(new Style().setFormatting(Formatting.RED).setItalic(true));
			this.notificationDialogTimer = 100; // 5 seconds (100 ticks)

			return;
		}

		this.importPackOptions(paths.get(0));
	}

	public void importPackOptions(Path settingFile) {
		try (InputStream in = Files.newInputStream(settingFile)) {
			Properties properties = new Properties();
			properties.load(in);

			Iris.queueShaderPackOptionsFromProperties(properties);

			this.notificationDialog = new TranslatableText(
					"options.iris.shaderPackOptions.importedSettings",
					settingFile.getFileName().toString()
			).setStyle(new Style().setFormatting(Formatting.YELLOW).setItalic(true));
			this.notificationDialogTimer = 100; // 5 seconds (100 ticks)

			if (this.navigation != null) {
				this.navigation.refresh();
			}
		} catch (Exception e) {
			// If the file could not be properly parsed or loaded,
			// log the error and display a message to the user
			Iris.logger.error("Error importing shader settings file \""+ settingFile.toString() +"\"", e);

			this.notificationDialog = new TranslatableText(
					"options.iris.shaderPackOptions.failedImport",
					settingFile.getFileName().toString()
			).setStyle(new Style().setFormatting(Formatting.RED).setItalic(true));
			this.notificationDialogTimer = 100; // 5 seconds (100 ticks)
		}
	}

	@Override
	public void removed() {
		if (!dropChanges) {
			applyChanges();
		} else {
			discardChanges();
		}
    }

	private void dropChangesAndClose() {
		dropChanges = true;
        this.client.setScreen(parent);
	}

	public void applyChanges() {
		ShaderPackSelectionList.BaseEntry base = this.shaderPackList.getSelected();

		if (!(base instanceof ShaderPackSelectionList.ShaderPackEntry)) {
			return;
		}

		ShaderPackSelectionList.ShaderPackEntry entry = (ShaderPackSelectionList.ShaderPackEntry)base;
		this.shaderPackList.setApplied(entry);

		String name = entry.getPackName();

		// If the pack is being changed, clear pending options from the previous pack to
		// avoid possible undefined behavior from applying one pack's options to another pack
		if (!name.equals(Iris.getCurrentPackName())) {
			Iris.clearShaderPackOptionQueue();
		}

		boolean enabled = this.shaderPackList.getTopButtonRow().shadersEnabled;

		String previousPackName = Iris.getIrisConfig().getShaderPackName().orElse(null);
		boolean previousShadersEnabled = Iris.getIrisConfig().areShadersEnabled();

		// Only reload if the pack would be different from before, or shaders were toggled, or options were changed, or if we're about to reset options.
		if (!name.equals(previousPackName) || enabled != previousShadersEnabled || !Iris.getShaderPackOptionQueue().isEmpty() || Iris.shouldResetShaderPackOptionsOnNextReload()) {
			Iris.getIrisConfig().setShaderPackName(name);
		}

		refreshForChangedPack();
	}

	private void discardChanges() {
		Iris.clearShaderPackOptionQueue();
	}

	private void openShaderPackFolder() {
        try {
            Desktop.getDesktop().open(Iris.getShaderpacksDirectoryManager().getRoot().toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	// Let the screen know if an element is hovered or not, allowing for accurately updating which element is hovered
	public void setElementHoveredStatus(AbstractElementWidget<?> widget, boolean hovered) {
		if (hovered && widget != this.hoveredElement) {
			this.hoveredElement = widget;

			if (widget instanceof CommentedElementWidget) {
				this.hoveredElementCommentTitle = ((CommentedElementWidget<?>) widget).getCommentTitle();

				Optional<Text> commentBody = ((CommentedElementWidget<?>) widget).getCommentBody();
				if (!commentBody.isPresent()) {
					this.hoveredElementCommentBody.clear();
				} else {
					String rawCommentBody = commentBody.get().asFormattedString();

					// Strip any trailing "."s
					if (rawCommentBody.endsWith(".")) {
						rawCommentBody = rawCommentBody.substring(0, rawCommentBody.length() - 1);
					}
					// Split comment body into lines by separator ". "
					List<Text> splitByPeriods = Arrays.stream(rawCommentBody.split("\\. [ ]*")).map(LiteralText::new).collect(Collectors.toList());
					// Line wrap
					this.hoveredElementCommentBody = new ArrayList<>();
					for (Text text : splitByPeriods) {
						this.hoveredElementCommentBody.addAll(this.textRenderer.wrapLines(text.asFormattedString(), COMMENT_PANEL_WIDTH - 8));
					}
				}
			} else {
				this.hoveredElementCommentTitle = Optional.empty();
				this.hoveredElementCommentBody.clear();
			}

			this.hoveredElementCommentTimer = 0;
		} else if (!hovered && widget == this.hoveredElement) {
			this.hoveredElement = null;
			this.hoveredElementCommentTitle = Optional.empty();
			this.hoveredElementCommentBody.clear();
			this.hoveredElementCommentTimer = 0;
		}
	}

	public boolean isDisplayingComment() {
		return this.hoveredElementCommentTimer > 20 &&
				this.hoveredElementCommentTitle.isPresent() &&
				!this.hoveredElementCommentBody.isEmpty();
	}
}
