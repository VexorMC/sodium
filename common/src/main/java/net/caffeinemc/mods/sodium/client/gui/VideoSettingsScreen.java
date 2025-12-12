package net.caffeinemc.mods.sodium.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.vexor.radium.compat.mojang.minecraft.gui.Renderable;
import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.IntegerOption;
import net.caffeinemc.mods.sodium.client.config.structure.Option;
import net.caffeinemc.mods.sodium.client.config.structure.OptionPage;
import net.caffeinemc.mods.sodium.client.data.fingerprint.HashedFingerprint;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.gui.screen.ConfigCorruptedScreen;
import net.caffeinemc.mods.sodium.client.gui.screen.RenderableScreen;
import net.caffeinemc.mods.sodium.client.gui.widgets.*;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.VideoOptionsScreen;
import net.minecraft.client.util.Window;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class VideoSettingsScreen extends RenderableScreen {
    public final Screen prevScreen;

    private PageListWidget pageList;
    private SearchWidget searchWidget;
    private OptionListWidget optionList;

    private FlatButtonWidget applyButton, closeButton, undoButton;
    private DonationButtonWidget donateButton;

    private boolean hasPendingChanges;

    private final ScrollableTooltip tooltip = new ScrollableTooltip(this);

    public VideoSettingsScreen(Screen prevScreen) {
        this.prevScreen = prevScreen;

        this.checkPromptTimers();

        // the binding values may have been modified in the meantime, reload from binding to update
        ConfigManager.CONFIG.resetAllOptionsFromBindings();
    }

    private void checkPromptTimers() {
        // Never show the prompt in developer workspaces.
        if (PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment()) {
            return;
        }

        var options = SodiumClientMod.options();

        // If the user has already seen the prompt, don't show it again.
        if (options.notifications.hasSeenDonationPrompt) {
            return;
        }

        HashedFingerprint fingerprint = null;

        try {
            fingerprint = HashedFingerprint.loadFromDisk();
        } catch (Throwable t) {
            SodiumClientMod.logger()
                    .error("Failed to read the fingerprint from disk", t);
        }

        // If the fingerprint doesn't exist, or failed to be loaded, abort.
        if (fingerprint == null) {
            return;
        }

        // The fingerprint records the installation time. If it's been a while since installation, show the user
        // a prompt asking for them to consider donating.
        var now = Instant.now();
        var threshold = Instant.ofEpochSecond(fingerprint.timestamp())
                .plus(3, ChronoUnit.DAYS);

        if (now.isAfter(threshold)) {
            this.openDonationPrompt(options);
        }
    }

    private void openDonationPrompt(SodiumOptions options) {
        options.notifications.hasSeenDonationPrompt = true;

        try {
            SodiumOptions.writeToDisk(options);
        } catch (IOException e) {
            SodiumClientMod.logger()
                    .error("Failed to update config file", e);
        }
    }

    public static Screen createScreen(Screen currentScreen) {
        if (SodiumClientMod.options().isReadOnly()) {
            return new ConfigCorruptedScreen(currentScreen, VideoSettingsScreen::new);
        } else {
            return new VideoSettingsScreen(currentScreen);
        }
    }

    @Override
    public void init() {
        super.init();

        ConfigManager.CONFIG.invalidateGlobalRebuildDependents();
        this.rebuild();
    }

    private void rebuild() {
        this.clearWidgets();

        int topBarHeight = Layout.BUTTON_SHORT;
        this.searchWidget = new SearchWidget(this::onSearchResults, new Dim2i(0, 0, this.width, topBarHeight));

        this.pageList = new PageListWidget(new Dim2i(0, topBarHeight, Layout.PAGE_LIST_WIDTH, this.height - topBarHeight), this);
        this.addRenderableWidget(this.pageList);

        boolean stackVertically = false;
        boolean reserveBottomSpace = false;

        int minWidthToStack = Layout.PAGE_LIST_WIDTH + Layout.INNER_MARGIN * 2 + Layout.OPTION_WIDTH + Layout.OPTION_LIST_SCROLLBAR_OFFSET + Layout.SCROLLBAR_WIDTH + Layout.BUTTON_LONG;
        int maxWidthToStack = minWidthToStack + Layout.BUTTON_LONG * 2 + Layout.INNER_MARGIN;

        if (this.width > minWidthToStack && this.width < maxWidthToStack) {
            stackVertically = true;
        } else if (this.width < minWidthToStack) {
            reserveBottomSpace = true;
        }

        this.closeButton = new FlatButtonWidget(new Dim2i(this.width - Layout.BUTTON_LONG - Layout.INNER_MARGIN, this.height - (Layout.INNER_MARGIN + Layout.BUTTON_SHORT), Layout.BUTTON_LONG, Layout.BUTTON_SHORT), new TranslatableText("gui.done"), this::removed, true, false);
        this.addRenderableWidget(this.closeButton);

        if (stackVertically) {
            this.applyButton = new FlatButtonWidget(new Dim2i(this.closeButton.getX(), this.closeButton.getY() - (Layout.INNER_MARGIN + Layout.BUTTON_SHORT), Layout.BUTTON_LONG, Layout.BUTTON_SHORT), new TranslatableText("sodium.options.buttons.apply"), ConfigManager.CONFIG::applyAllOptions, true, false);
            this.undoButton = new FlatButtonWidget(new Dim2i(this.applyButton.getX(), this.applyButton.getY() - (Layout.INNER_MARGIN + Layout.BUTTON_SHORT), Layout.BUTTON_LONG, Layout.BUTTON_SHORT), new TranslatableText("sodium.options.buttons.undo"), this::undoChanges, true, false);
        } else {
            this.applyButton = new FlatButtonWidget(new Dim2i(this.closeButton.getX() - Layout.INNER_MARGIN - Layout.BUTTON_LONG, this.height - (Layout.INNER_MARGIN + Layout.BUTTON_SHORT), Layout.BUTTON_LONG, Layout.BUTTON_SHORT), new TranslatableText("sodium.options.buttons.apply"), ConfigManager.CONFIG::applyAllOptions, true, false);
            this.undoButton = new FlatButtonWidget(new Dim2i(this.applyButton.getX() - Layout.INNER_MARGIN - Layout.BUTTON_LONG, this.height - (Layout.INNER_MARGIN + Layout.BUTTON_SHORT), Layout.BUTTON_LONG, Layout.BUTTON_SHORT), new TranslatableText("sodium.options.buttons.undo"), this::undoChanges, true, false);
        }
        this.addRenderableWidget(this.undoButton);
        this.addRenderableWidget(this.applyButton);

        this.donateButton = new DonationButtonWidget(this, this.width, this::openDonationPage, this::hideDonationButton);
        this.addRenderableWidget(this.searchWidget);
        this.updateSearchWidgetWidth();
        Window window = new Window(client);

        var optionListDim = new Dim2i(
                this.pageList.getLimitX(),
                topBarHeight + Layout.INNER_MARGIN,
                Layout.OPTION_WIDTH + Layout.OPTION_LIST_SCROLLBAR_OFFSET + Layout.SCROLLBAR_WIDTH,
                window.getHeight() - topBarHeight - (reserveBottomSpace ? (Layout.INNER_MARGIN * 3 + Layout.BUTTON_SHORT) : (Layout.INNER_MARGIN * 2))
        );
        this.optionList = new OptionListWidget(this, optionListDim, this::onSectionFocused);
        this.addRenderableWidget(this.optionList);
    }

    private void onSearchResults(List<Option.OptionNameSource> searchResults) {
        if (searchResults.isEmpty()) {
            this.optionList.clearFilter();
        } else {
            this.optionList.setFilteredOptions(searchResults);
        }
        this.optionList.rebuild(this);
    }

    private void onSectionFocused(OptionPage page) {
        this.pageList.switchSelected(page);
    }

    public void jumpToPage(OptionPage page) {
        if (this.optionList != null) {
            this.optionList.jumpToPage(page);
        }
    }

    private void updateSearchWidgetWidth() {
        this.searchWidget.updateWidgetWidth(this.width - this.donateButton.getWidth());
    }

    private void hideDonationButton() {
        SodiumOptions options = SodiumClientMod.options();
        options.notifications.hasClearedDonationButton = true;

        try {
            SodiumOptions.writeToDisk(options);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }

        this.donateButton.updateDisplay(this, false);
        this.updateSearchWidgetWidth();
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.updateControls(mouseX, mouseY);

        super.render(mouseX, mouseY, delta);

        this.tooltip.render();
    }

    private void updateControls(int mouseX, int mouseY) {
        boolean hasChanges = ConfigManager.CONFIG.anyOptionChanged();

        this.applyButton.setEnabled(hasChanges);
        this.undoButton.setVisible(hasChanges);
        this.closeButton.setEnabled(!hasChanges);

        AbstractWidget reservedAreaBlocker;
        if (hasChanges) {
            reservedAreaBlocker = this.undoButton;
        } else {
            reservedAreaBlocker = this.applyButton;
        }
        this.tooltip.setReservedAreaTopLeftCorner(reservedAreaBlocker.getX(), reservedAreaBlocker.getY());

        this.hasPendingChanges = hasChanges;

        // determine the tooltip hover target
        // this is the first item that's hovered over, or if nothing is hovered, the focused item
        ControlElement hovered = null;
        ControlElement focused = null;
        if (mouseX >= this.optionList.getX() && mouseX <= this.optionList.getLimitX() &&
                mouseY >= this.optionList.getY() && mouseY <= this.optionList.getLimitY()) {
            for (ControlElement element : this.optionList.getControls()) {
                if (element.isMouseOver(mouseX, mouseY)) {
                    hovered = element;
                    break;
                }
                if (element.isFocused()) {
                    focused = element;
                }
            }
        }
        var hoverTarget = hovered != null ? hovered : focused;

        this.tooltip.onControlHover(hoverTarget, mouseX, mouseY);
    }

    private void undoChanges() {
        ConfigManager.CONFIG.resetAllOptionsFromBindings();
    }

    private void openDonationPage() {
        try {
            Desktop.getDesktop().browse(URI.create("https://caffeinemc.net/donate"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void keyPressed(char id, int code) {
        if (code == Keyboard.KEY_ESCAPE && this.hasPendingChanges) {
            // prevent closing the screen with pending changes
            return;
        }

        if (!this.searchWidget.isSearching()) {
            if (code == Keyboard.KEY_P && Screen.hasShiftDown()) {
                this.client.setScreen(new VideoOptionsScreen(this.prevScreen, this.client.options));
                return;
            }

            if (code == Keyboard.KEY_T) {
                this.searchWidget.setFocused(true);
            }
        }

        super.keyPressed(id, code);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double f, double amount) {
        // change the gui scale with scrolling if the control key is held
        if (Screen.hasControlDown()) {
            var location = new Identifier("radium:general.gui_scale");
            var option = ConfigManager.CONFIG.getOption(location);
            if (option instanceof IntegerOption guiScaleOption) {
                var intValue = guiScaleOption.getValidatedValue();
                if (intValue instanceof Integer) {
                    var range = guiScaleOption.getRange();
                    var top = range.max() + 1;
                    var auto = range.min();

                    // re-maps the auto value (presumably 0) to be at the top of the scroll range
                    if (intValue == auto) {
                        intValue = top;
                    }
                    var newValue = MathHelper.clamp(intValue + (int) Math.signum(amount), auto + 1, top);
                    if (newValue != intValue) {
                        if (newValue == top) {
                            newValue = auto;
                        }
                        if (range.isValueValid(newValue)) {
                            guiScaleOption.modifyValue(newValue);
                            ConfigManager.CONFIG.applyOption(location);
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        if (this.tooltip.mouseScrolled(x, y, amount)) {
            return true;
        }

        return super.mouseScrolled(x, y, f, amount);
    }

    public <T extends GuiEventListener & Renderable> T addRenderableWidget(T guiEventListener) {
        return super.addRenderableWidget(guiEventListener);
    }

    public void removeWidget(GuiEventListener guiEventListener) {
        super.removeWidget(guiEventListener);
    }

    public <T extends GuiEventListener & Renderable> void setWidgetPresence(T guiEventListener, boolean present) {
        this.removeWidget(guiEventListener);
        if (present) {
            this.addRenderableWidget(guiEventListener);
        }
    }

    @Override
    public void removed() {
    }

    public static void renderIcon(Identifier icon, int color, int x, int y, int size) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(icon);
        GlStateManager.color(
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                ((color >> 24) & 0xFF) / 255f
        );
        DrawableHelper.drawTexture(x, y, 0, 0, size, size, size, size);
    }

    public static int renderIconWithSpacing(Identifier icon, int color, int x, int y, int height, int margin) {
        int iconSize = height - margin * 2;

        renderIcon(icon, color, x + margin, y + height / 2 - iconSize / 2, iconSize);

        return margin * 2 + iconSize;
    }
}
