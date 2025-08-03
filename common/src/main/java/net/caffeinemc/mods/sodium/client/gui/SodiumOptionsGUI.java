package net.caffeinemc.mods.sodium.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.vexor.radium.extra.client.gui.SodiumExtraGameOptionPages;
import dev.vexor.radium.extra.client.gui.scrollable_page.OptionPageScrollFrame;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.data.fingerprint.HashedFingerprint;
import net.caffeinemc.mods.sodium.client.console.Console;
import net.caffeinemc.mods.sodium.client.console.message.MessageLevel;
import net.caffeinemc.mods.sodium.client.gui.options.*;
import net.caffeinemc.mods.sodium.client.gui.options.control.Control;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPrompt;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPromptable;
import net.caffeinemc.mods.sodium.client.gui.screen.ConfigCorruptedScreen;
import net.caffeinemc.mods.sodium.client.gui.screen.RenderableScreen;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

// TODO: Rename in Sodium 0.6
public class SodiumOptionsGUI extends RenderableScreen implements ScreenPromptable {
    public final List<OptionPage> pages = new ArrayList<>();

    private final List<ControlElement<?>> controls = new ArrayList<>();

    public final Screen prevScreen;

    private OptionPage currentPage;

    protected FlatButtonWidget applyButton, closeButton, undoButton;
    private FlatButtonWidget donateButton, hideDonateButton;

    private ControlElement<?> hoveredElement;

    protected boolean hasPendingChanges;
    private @Nullable ScreenPrompt prompt;

    public SodiumOptionsGUI(Screen prevScreen) {
        this.prevScreen = prevScreen;

        this.pages.add(SodiumGameOptionPages.general());
        this.pages.add(SodiumGameOptionPages.quality());
        this.pages.add(SodiumGameOptionPages.performance());
        this.pages.add(SodiumGameOptionPages.advanced());
        this.pages.add(SodiumGameOptionPages.particleCulling());

        this.pages.add(SodiumExtraGameOptionPages.animation());
        this.pages.add(SodiumExtraGameOptionPages.particle());
        this.pages.add(SodiumExtraGameOptionPages.detail());
        this.pages.add(SodiumExtraGameOptionPages.render());
        this.pages.add(SodiumExtraGameOptionPages.extra());

        this.checkPromptTimers();
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

    private void openDonationPrompt(SodiumGameOptions options) {
        var prompt = new ScreenPrompt(this, DONATION_PROMPT_MESSAGE.stream().map(Text::asFormattedString).toList(), 320, 190,
                new ScreenPrompt.Action(new LiteralText("Buy us a coffee"), this::openDonationPage));
        prompt.setFocused(true);

        options.notifications.hasSeenDonationPrompt = true;

        try {
            SodiumGameOptions.writeToDisk(options);
        } catch (IOException e) {
            SodiumClientMod.logger()
                    .error("Failed to update config file", e);
        }
    }

    public static Screen createScreen(Screen currentScreen) {
        if (SodiumClientMod.options().isReadOnly()) {
            return new ConfigCorruptedScreen(currentScreen, SodiumOptionsGUI::new);
        } else {
            return new SodiumOptionsGUI(currentScreen);
        }
    }

    public void setPage(OptionPage page) {
        this.currentPage = page;

        this.rebuildGUI();
    }

    @Override
    public void init() {
        super.init();

        this.rebuildGUI();

        if (this.prompt != null) {
            this.prompt.init();
        }
    }

    protected void rebuildGUI() {
        this.controls.clear();

        this.clearWidgets();

        if (this.currentPage == null) {
            if (this.pages.isEmpty()) {
                throw new IllegalStateException("No pages are available?!");
            }

            // Just use the first page for now
            this.currentPage = this.pages.get(0);
        }

        this.rebuildGUIPages();
        this.rebuildGUIOptions();

        this.undoButton = new FlatButtonWidget(new Dim2i(this.width - 211, this.height - 30, 65, 20), new TranslatableText("sodium.options.buttons.undo"), this::undoChanges);
        this.applyButton = new FlatButtonWidget(new Dim2i(this.width - 142, this.height - 30, 65, 20), new TranslatableText("sodium.options.buttons.apply"), this::applyChanges);
        this.closeButton = new FlatButtonWidget(new Dim2i(this.width - 73, this.height - 30, 65, 20), new TranslatableText("gui.done"), this::onClose);
        this.donateButton = new FlatButtonWidget(new Dim2i(this.width - 128, 6, 100, 20), new TranslatableText("sodium.options.buttons.donate"), this::openDonationPage);
        this.hideDonateButton = new FlatButtonWidget(new Dim2i(this.width - 26, 6, 20, 20), new LiteralText("x"), this::hideDonationButton);

        if (SodiumClientMod.options().notifications.hasClearedDonationButton) {
            this.setDonationButtonVisibility();
        }

        this.addRenderableWidget(this.undoButton);
        this.addRenderableWidget(this.applyButton);
        this.addRenderableWidget(this.closeButton);
        this.addRenderableWidget(this.donateButton);
        this.addRenderableWidget(this.hideDonateButton);
    }

    private void setDonationButtonVisibility() {
        this.donateButton.setVisible(false);
        this.hideDonateButton.setVisible(false);
    }

    private void hideDonationButton() {
        SodiumGameOptions options = SodiumClientMod.options();
        options.notifications.hasClearedDonationButton = true;

        try {
            SodiumGameOptions.writeToDisk(options);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }

        this.setDonationButtonVisibility();
    }

    private void rebuildGUIPages() {
        int x = 6;
        int y = 6;

        for (OptionPage page : this.pages) {
            int width = 12 + this.textRenderer.getStringWidth(page.getName().asFormattedString());

            FlatButtonWidget button = new FlatButtonWidget(new Dim2i(x, y, width, 18), page.getName(), () -> this.setPage(page));
            button.setSelected(this.currentPage == page);

            x += width + 6;

            this.addRenderableWidget(button);
        }
    }

    private void rebuildGUIOptions() {
        int x = 6;
        int y = 28;

        for (OptionGroup group : this.currentPage.getGroups()) {
            // Add each option's control element
            for (Option<?> option : group.getOptions()) {
                Control<?> control = option.getControl();
                ControlElement<?> element = control.createElement(new Dim2i(x, y, 240, 18));

                this.addRenderableWidget(element);

                OptionPageScrollFrame optionPageScrollFrame = new OptionPageScrollFrame(new Dim2i(x, y, /*this.width - x * 2*/240, this.height - y - 10 /*- 64*/), this.currentPage);
                this.addRenderableWidget(optionPageScrollFrame);


                this.controls.add(element);

                // Move down to the next option
                y += 18;
            }

            // Add padding beneath each option group
            y += 4;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.updateControls();

        super.render(this.prompt != null ? -1 : mouseX, this.prompt != null ? -1 : mouseY, delta);

        if (this.hoveredElement != null) {
            this.renderOptionTooltip(this.hoveredElement);
        }

        if (this.prompt != null) {
            this.prompt.render(mouseX, mouseY, delta);
        }
    }

    private void updateControls() {
        ControlElement<?> hovered = this.getActiveControls()
                .filter(ControlElement::isHovered)
                .findFirst()
                .orElse(this.getActiveControls() // If there is no hovered element, use the focused element.
                        .filter(ControlElement::isFocused)
                        .findFirst()
                        .orElse(null));

        boolean hasChanges = this.getAllOptions()
                .anyMatch(Option::hasChanged);

        for (OptionPage page : this.pages) {
            for (Option<?> option : page.getOptions()) {
                if (option.hasChanged()) {
                    hasChanges = true;
                }
            }
        }

        this.applyButton.setEnabled(hasChanges);
        this.undoButton.setVisible(hasChanges);
        this.closeButton.setEnabled(!hasChanges);

        this.hoveredElement = hovered;

        this.hasPendingChanges = hasChanges;
    }

    private Stream<Option<?>> getAllOptions() {
        return this.pages.stream()
                .flatMap(s -> s.getOptions().stream());
    }

    private Stream<ControlElement<?>> getActiveControls() {
        return this.controls.stream();
    }

    private void renderOptionTooltip(ControlElement<?> element) {
        Dim2i dim = element.getDimensions();

        int textPadding = 3;
        int boxPadding = 3;

        int boxY = dim.y();
        int boxX = dim.getLimitX() + boxPadding;

        int boxWidth = Math.min(200, this.width - boxX - boxPadding);

        Option<?> option = element.getOption();
        var splitWidth = boxWidth - (textPadding * 2);
        List<String> tooltip = new ArrayList<>(this.textRenderer.wrapLines(option.getTooltip().asFormattedString(),splitWidth));

        OptionImpact impact = option.getImpact();

        if (impact != null) {
            var impactText = new TranslatableText("sodium.options.performance_impact_string",
                    impact.getLocalizedName());
            tooltip.addAll(this.textRenderer.wrapLines(impactText.setStyle(new Style().setFormatting(Formatting.GRAY)).asFormattedString(), splitWidth));
        }

        int boxHeight = (tooltip.size() * 12) + boxPadding;
        int boxYLimit = boxY + boxHeight;
        int boxYCutoff = this.height - 40;

        // If the box is going to be cutoff on the Y-axis, move it back up the difference
        if (boxYLimit > boxYCutoff) {
            boxY -= boxYLimit - boxYCutoff;
        }

        fillGradient(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE0000000, 0xE0000000);

        for (int i = 0; i < tooltip.size(); i++) {
            this.textRenderer.draw(tooltip.get(i), boxX + textPadding, boxY + textPadding + (i * 12), 0xFFFFFFFF);
        }
    }

    protected void fillGradient(int x1, int y1, int x2, int y2, int color1, int color2) {
        float f = (float)(color1 >> 24 & 255) / 255.0F;
        float g = (float)(color1 >> 16 & 255) / 255.0F;
        float h = (float)(color1 >> 8 & 255) / 255.0F;
        float i = (float)(color1 & 255) / 255.0F;
        float j = (float)(color2 >> 24 & 255) / 255.0F;
        float k = (float)(color2 >> 16 & 255) / 255.0F;
        float l = (float)(color2 >> 8 & 255) / 255.0F;
        float m = (float)(color2 & 255) / 255.0F;
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex((double)x2, (double)y1, (double)this.zOffset).color(g, h, i, f).next();
        bufferBuilder.vertex((double)x1, (double)y1, (double)this.zOffset).color(g, h, i, f).next();
        bufferBuilder.vertex((double)x1, (double)y2, (double)this.zOffset).color(k, l, m, j).next();
        bufferBuilder.vertex((double)x2, (double)y2, (double)this.zOffset).color(k, l, m, j).next();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }


    private void applyChanges() {
        final HashSet<OptionStorage<?>> dirtyStorages = new HashSet<>();
        final EnumSet<OptionFlag> flags = EnumSet.noneOf(OptionFlag.class);

        this.getAllOptions().forEach((option -> {
            if (!option.hasChanged()) {
                return;
            }

            option.applyChanges();

            flags.addAll(option.getFlags());
            dirtyStorages.add(option.getStorage());
        }));

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world != null) {
            if (flags.contains(OptionFlag.REQUIRES_RENDERER_RELOAD)) {
                client.worldRenderer.reload();
            } else if (flags.contains(OptionFlag.REQUIRES_RENDERER_UPDATE)) {
                client.worldRenderer.scheduleTerrainUpdate();
            }
        }

        if (flags.contains(OptionFlag.REQUIRES_ASSET_RELOAD)) {
            client.reloadResources();
        }

        if (flags.contains(OptionFlag.REQUIRES_GAME_RESTART)) {
            Console.instance().logMessage(MessageLevel.WARN,
                    "sodium.console.game_restart", true, 10.0);
        }

        for (OptionStorage<?> storage : dirtyStorages) {
            storage.save();
        }
    }

    private void undoChanges() {
        this.getAllOptions()
                .forEach(Option::reset);
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
        if (this.prompt != null && this.prompt.keyPressed(code, id)) {
        } else {
            super.keyPressed(id, code);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (this.prompt != null) {
            this.prompt.mouseClicked(mouseX, mouseY, button);
        } else {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void onClose() {
        this.client.setScreen(this.prevScreen);
    }

    @Override
    public void setPrompt(@Nullable ScreenPrompt prompt) {
        this.prompt = prompt;
    }

    @Nullable
    @Override
    public ScreenPrompt getPrompt() {
        return this.prompt;
    }

    @Override
    public Dim2i getDimensions() {
        return new Dim2i(0, 0, this.width, this.height);
    }

    private static final List<Text> DONATION_PROMPT_MESSAGE;

    static {
        DONATION_PROMPT_MESSAGE = List.of(
                new LiteralText("Hello!"),
                new LiteralText("It seems that you've been enjoying "),
                new LiteralText("Sodium"),
                new LiteralText(", the powerful and open rendering optimization mod for Minecraft."),
                new LiteralText("Mods like these are complex. They require "),
                new LiteralText("thousands of hours"), 
                new LiteralText(" of development, debugging, and tuning to create the experience that players have come to expect."),
                new LiteralText("If you'd like to show your token of appreciation, and support the development of our mod in the process, then consider "),
                new LiteralText("buying us a coffee"),
                new LiteralText("."),
                new LiteralText("And thanks again for using our mod! We hope it helps you (and your computer.)")
        );
    }

    public boolean shouldCloseOnEsc() {
        return !this.hasPendingChanges;
    }
}
