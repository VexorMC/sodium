package me.flashyreese.mods.reeses_sodium_options.client.gui;

import dev.vexor.radium.compat.mojang.minecraft.gui.event.GuiEventListener;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.AbstractFrame;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.BasicFrame;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.components.SearchTextFieldComponent;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.tab.Tab;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.tab.TabFrame;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptionsGUI;
import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.gui.options.OptionFlag;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;
import net.caffeinemc.mods.sodium.client.gui.screen.RenderableScreen;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.VideoOptionsScreen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ReeseSodiumVideoOptionsScreen extends SodiumOptionsGUI {
    @Nullable
    private GuiEventListener focused;

    private static final AtomicReference<Text> tabFrameSelectedTab = new AtomicReference<>(null);
    private static final AtomicReference<Integer> tabFrameScrollBarOffset = new AtomicReference<>(0);
    private static final AtomicReference<Integer> optionPageScrollBarOffset = new AtomicReference<>(0);

    private static final AtomicReference<String> lastSearch = new AtomicReference<>("");
    private static final AtomicReference<Integer> lastSearchIndex = new AtomicReference<>(0);

    private AbstractFrame frame;
    private SearchTextFieldComponent searchTextField;

    public ReeseSodiumVideoOptionsScreen(Screen prevScreen) {
        super(prevScreen);
    }

    // Hackalicious! Rebuild UI
    @Override
    public void rebuildGUI() {
        this.widgets.clear();
        this.init();
    }


    public void setFocused(@Nullable GuiEventListener focused) {
        this.focused = focused;
    }

    @Nullable
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void init() {
        this.frame = this.parentFrameBuilder().build();
        this.widgets.add(this.frame);

        this.searchTextField.setFocused(!lastSearch.get().trim().isEmpty());
        if (this.searchTextField.isFocused()) {
            this.setFocused(this.searchTextField);
        } else {
            this.setFocused(this.frame);
        }
    }

    protected BasicFrame.Builder parentFrameBuilder() {
        final BasicFrame.Builder basicFrameBuilder;

        // Calculates if resolution exceeds 16:9 ratio, force 16:9
        int newWidth = this.width;
        if ((float) this.width / (float) this.height > 1.77777777778) {
            newWidth = (int) (this.height * 1.77777777778);
        }

        final Dim2i basicFrameDim = new Dim2i((this.width - newWidth) / 2, 0, newWidth, this.height);
        final Dim2i tabFrameDim = new Dim2i(basicFrameDim.getOriginX() + basicFrameDim.getWidth() / 20 / 2, basicFrameDim.getOriginY() + basicFrameDim.getHeight() / 4 / 2, basicFrameDim.getWidth() - (basicFrameDim.getWidth() / 20), basicFrameDim.getHeight() / 4 * 3);

        this.undoButton = new FlatButtonWidget(new Dim2i(this.width - 211, this.height - 30, 65, 20), new TranslatableText("sodium.options.buttons.undo"), this::undoChanges);
        this.applyButton = new FlatButtonWidget(new Dim2i(this.width - 142, this.height - 30, 65, 20), new TranslatableText("sodium.options.buttons.apply"), this::applyChanges);
        this.closeButton = new FlatButtonWidget(new Dim2i(this.width - 73, this.height - 30, 65, 20), new TranslatableText("gui.done"), this::onClose);

        Dim2i searchTextFieldDim = new Dim2i(tabFrameDim.getOriginX(), tabFrameDim.getOriginY() - 26, tabFrameDim.getWidth(), 20);

        basicFrameBuilder = this.parentBasicFrameBuilder(basicFrameDim, tabFrameDim);

        this.searchTextField = new SearchTextFieldComponent(searchTextFieldDim, this.pages, tabFrameSelectedTab,
                tabFrameScrollBarOffset, optionPageScrollBarOffset, tabFrameDim.getHeight(), this, lastSearch, lastSearchIndex);

        basicFrameBuilder.addChild(dim -> this.searchTextField);

        return basicFrameBuilder;
    }

    public BasicFrame.Builder parentBasicFrameBuilder(Dim2i parentBasicFrameDim, Dim2i tabFrameDim) {
        return BasicFrame.createBuilder()
                .setDimension(parentBasicFrameDim)
                .shouldRenderOutline(false)
                .addChild(parentDim -> TabFrame.createBuilder()
                        .setDimension(tabFrameDim)
                        .shouldRenderOutline(false)
                        .setTabSectionScrollBarOffset(tabFrameScrollBarOffset)
                        .setTabSectionSelectedTab(tabFrameSelectedTab)
                        .addTabs(tabs -> this.pages
                                .stream()
                                .filter(page -> !page.getGroups().isEmpty())
                                .forEach(page -> tabs.add(Tab.createBuilder().from(page, optionPageScrollBarOffset)))
                        )
                        .onSetTab(() -> {
                            optionPageScrollBarOffset.set(0);
                        })
                        .build()
                )
                .addChild(dim -> this.undoButton)
                .addChild(dim -> this.applyButton)
                .addChild(dim -> this.closeButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.renderBackground();
        this.updateControls();
        this.frame.render(mouseX, mouseY, partialTicks);
    }

    private void updateControls() {
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

        this.hasPendingChanges = hasChanges;
    }

    private Stream<Option<?>> getAllOptions() {
        return this.pages.stream().flatMap(s -> s.getOptions().stream());
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


        if (flags.contains(OptionFlag.REQUIRES_RENDERER_RELOAD)) {
            this.client.worldRenderer.reload();
        }

        if (flags.contains(OptionFlag.REQUIRES_ASSET_RELOAD)) {
            this.client.getSpriteAtlasTexture().setMaxTextureSize(this.client.options.mipmapLevels);
            this.client.reloadResources();
        }

        for (OptionStorage<?> storage : dirtyStorages) {
            storage.save();
        }
    }

    private void undoChanges() {
        this.getAllOptions().forEach(Option::reset);
    }

    @Override
    public void keyPressed(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE && !shouldCloseOnEsc()) {
            return;
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            onClose();
            return;
        }

        if(focused != null) {
            focused.keyPressed(keyCode, typedChar);
        }

        if (keyCode == Keyboard.KEY_P && hasShiftDown()) {
            this.client.setScreen(new VideoOptionsScreen(this.prevScreen, this.client.options));
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !this.hasPendingChanges;
    }

    @Override
    public void onClose() {
        lastSearch.set("");
        lastSearchIndex.set(0);
        super.onClose();
    }
}
