package net.caffeinemc.mods.sodium.client.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SearchTextFieldWidget extends AbstractWidget {
    private final Dim2i dim;
    private final TextRenderer textRenderer;
    private final Predicate<String> textPredicate = Objects::nonNull;
    private final Consumer<String> onChanged;
    private final Text hint;

    // References from original constructor
    private final AtomicReference<String> lastSearchRef;
    private final AtomicReference<Integer> lastSearchIndexRef;

    protected boolean selecting;
    protected String text = "";
    protected int maxLength = 200;
    protected boolean visible = true;
    protected boolean editable = true;

    private int firstCharacterIndex;
    private int selectionStart;
    private int selectionEnd;
    private boolean focused;

    public SearchTextFieldWidget(Dim2i dim, Consumer<String> onChanged, Text hint,
                                 AtomicReference<String> lastSearch,
                                 AtomicReference<Integer> lastSearchIndex) {
        super(dim);
        this.dim = dim;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.onChanged = onChanged;
        this.hint = hint;
        this.lastSearchRef = lastSearch;
        this.lastSearchIndexRef = lastSearchIndex;

        // Initialize with previous search if available
        if (lastSearch != null && !lastSearch.get().trim().isEmpty()) {
            this.write(lastSearch.get());
        }
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        return dim.containsCursor(x, y);
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        if (this.textPredicate.test(text)) {
            if (text.length() > this.maxLength) {
                this.text = text.substring(0, this.maxLength);
            } else {
                this.text = text;
            }
            this.setCursorToEnd();
            this.setSelectionEnd(this.selectionStart);
            this.onTextChanged(this.text);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) {
            return;
        }

        // Draw hint text when empty and not focused
        if (!this.isFocused() && this.text.isEmpty() && this.hint != null) {
            this.textRenderer.draw(this.hint.asFormattedString(), this.dim.x(), this.dim.y(), 0xFFAAAAAA);
        }

        // Draw text content
        final int j = this.selectionStart - this.firstCharacterIndex;
        int k = this.selectionEnd - this.firstCharacterIndex;
        final String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
        final boolean bl = j >= 0 && j <= string.length();
        final int l = this.dim.x();
        final int m = this.dim.y();
        int n = l;

        if (k > string.length()) {
            k = string.length();
        }

        if (!string.isEmpty()) {
            final String string2 = bl ? string.substring(0, j) : string;
            n = this.textRenderer.drawWithShadow(string2, n, m, 0xE0E0E0);
        }

        final boolean bl3 = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
        int o = n;
        if (!bl) {
            o = j > 0 ? l + this.dim.width() : l;
        } else if (bl3) {
            --o;
            --n;
        }

        if (!string.isEmpty() && bl && j < string.length()) {
            this.textRenderer.drawWithShadow(string.substring(j), n, m, 0xE0E0E0);
        }

        // Draw cursor
        if (this.isFocused() && bl) {
            if (bl3) {
                drawRect(o, m - 1, o + 1, m + 1 + this.textRenderer.fontHeight, -0x2F2F30);
            } else {
                this.textRenderer.drawWithShadow("_", o, m, 0xE0E0E0);
            }
        }

        // Draw selection highlight
        if (k != j) {
            final int p = l + this.textRenderer.getStringWidth(string.substring(0, k));
            this.drawSelectionHighlight(o, m - 1, p - 1, m + 1 + this.textRenderer.fontHeight);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isVisible()) {
            return false;
        }

        boolean wasInBounds = this.dim.containsCursor(mouseX, mouseY);
        this.setFocused(wasInBounds);

        if (this.isFocused() && button == 0) {
            final int i = MathHelper.floor(mouseX) - this.dim.x();
            final String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
            this.setCursor(this.textRenderer.trimToWidth(string, i).length() + this.firstCharacterIndex);
            return true;
        }

        return wasInBounds;
    }

    private void drawSelectionHighlight(int x1, int y1, int x2, int y2) {
        int i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        if (x2 > this.dim.x() + this.dim.width()) {
            x2 = this.dim.x() + this.dim.width();
        }
        if (x1 > this.dim.x() + this.dim.width()) {
            x1 = this.dim.x() + this.dim.width();
        }

        GlStateManager.enableColorLogic();
        GlStateManager.logicOp(GL11.GL_OR_REVERSE);
        drawRect(x1, y1, x2, y2, -0xFFFF01);
        GlStateManager.disableColorLogic();
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public String getSelectedText() {
        final int i = Math.min(this.selectionStart, this.selectionEnd);
        final int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.text.substring(i, j);
    }

    public void write(String text) {
        final int i = Math.min(this.selectionStart, this.selectionEnd);
        final int j = Math.max(this.selectionStart, this.selectionEnd);
        final int k = this.maxLength - this.text.length() - (i - j);
        String string = text;
        int l = string.length();
        if (k < l) {
            string = string.substring(0, k);
            l = k;
        }

        final String string2 = (new StringBuilder(this.text)).replace(i, j, string).toString();
        if (this.textPredicate.test(string2)) {
            // Save previous query
            if (this.lastSearchRef != null) {
                this.lastSearchRef.set(this.text.trim());
            }
            this.text = string2;
            this.setSelectionStart(i + l);
            this.setSelectionEnd(this.selectionStart);
            this.resetSearchIndex(); // Reset index when typing new text
            this.onTextChanged(this.text);
        }
    }

    private void onTextChanged(String newText) {
        // Update last query tracking
        if (!newText.equals(this.text) && this.lastSearchRef != null) {
            this.lastSearchRef.set(this.text.trim());
        }

        if (this.onChanged != null) {
            this.onChanged.accept(newText);
        }
    }

    private void erase(int offset) {
        if (Screen.hasControlDown()) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }
    }

    public void eraseWords(int wordOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                this.eraseCharacters(this.getWordSkipPosition(wordOffset) - this.selectionStart);
            }
        }
    }

    public void eraseCharacters(int characterOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                final int i = this.getCursorPosWithOffset(characterOffset);
                final int j = Math.min(i, this.selectionStart);
                final int k = Math.max(i, this.selectionStart);
                if (j != k) {
                    final String string = (new StringBuilder(this.text)).delete(j, k).toString();
                    if (this.textPredicate.test(string)) {
                        this.text = string;
                        this.setCursor(j);
                        this.onTextChanged(this.text);
                    }
                }
            }
        }
    }

    public int getWordSkipPosition(int wordOffset) {
        return this.getWordSkipPosition(wordOffset, this.getCursor());
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition) {
        return this.getWordSkipPosition(wordOffset, cursorPosition, true);
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
        int i = cursorPosition;
        final boolean bl = wordOffset < 0;
        final int j = Math.abs(wordOffset);

        for (int k = 0; k < j; ++k) {
            if (!bl) {
                final int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public int getCursor() {
        return this.selectionStart;
    }

    public void setCursor(int cursor) {
        this.setSelectionStart(cursor);
        if (!this.selecting) {
            this.setSelectionEnd(this.selectionStart);
        }
        this.onTextChanged(this.text);
    }

    public void moveCursor(int offset) {
        this.setCursor(this.getCursorPosWithOffset(offset));
    }

    private int getCursorPosWithOffset(int offset) {
        return this.selectionStart + offset;
    }

    public void setSelectionStart(int cursor) {
        this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
    }

    public void setCursorToStart() {
        this.setCursor(0);
    }

    public void setCursorToEnd() {
        this.setCursor(this.text.length());
    }

    public void setSelectionEnd(int index) {
        final int i = this.text.length();
        this.selectionEnd = MathHelper.clamp(index, 0, i);
        if (this.textRenderer != null) {
            if (this.firstCharacterIndex > i) {
                this.firstCharacterIndex = i;
            }

            final int j = this.getInnerWidth();
            final String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), j);
            final int k = string.length() + this.firstCharacterIndex;
            if (this.selectionEnd == this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.textRenderer.trimToWidth(this.text, j, true).length();
            }

            if (this.selectionEnd > k) {
                this.firstCharacterIndex += this.selectionEnd - k;
            } else if (this.selectionEnd <= this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.firstCharacterIndex - this.selectionEnd;
            }

            this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, i);
        }
    }

    public boolean isActive() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    @Override
    public boolean keyPressed(int keyCode, char typedChar) {
        if (!this.isActive()) {
            return false;
        }

        this.selecting = Screen.hasShiftDown();

        if (keyCode == Keyboard.KEY_A && Screen.hasControlDown() && !Screen.hasShiftDown()) {
            // Select all
            this.setCursorToEnd();
            this.setSelectionEnd(0);
            return true;
        } else if (keyCode == Keyboard.KEY_C && Screen.hasControlDown() && !Screen.hasShiftDown()) {
            // Copy
            Screen.setClipboard(this.getSelectedText());
            return true;
        } else if (keyCode == Keyboard.KEY_V && Screen.hasControlDown() && !Screen.hasShiftDown()) {
            // Paste
            if (this.editable) {
                this.write(Screen.getClipboard());
            }
            return true;
        } else if (keyCode == Keyboard.KEY_X && Screen.hasControlDown() && !Screen.hasShiftDown()) {
            // Cut
            Screen.setClipboard(this.getSelectedText());
            if (this.editable) {
                this.write("");
            }
            return true;
        } else {
            switch (keyCode) {
                case 28: // Enter - cycle through search results
                    if (this.editable && this.hasQuery()) {
                        // The parent widget should handle result cycling
                        // by checking getLastSearchIndex() and incrementing it
                        return true;
                    }
                    return false;
                case 14: // Backspace
                    if (this.editable) {
                        this.selecting = false;
                        this.erase(-1);
                        this.selecting = Screen.hasShiftDown();
                        this.resetSearchIndex(); // Reset when editing
                    }
                    return true;
                case 211: // Delete
                    if (this.editable) {
                        this.selecting = false;
                        this.erase(1);
                        this.selecting = Screen.hasShiftDown();
                        this.resetSearchIndex(); // Reset when editing
                    }
                    return true;
                case 205: // Right arrow
                    if (Screen.hasControlDown()) {
                        this.setCursor(this.getWordSkipPosition(1));
                    } else {
                        this.moveCursor(1);
                    }
                    return true;
                case 203: // Left arrow
                    if (Screen.hasControlDown()) {
                        this.setCursor(this.getWordSkipPosition(-1));
                    } else {
                        this.moveCursor(-1);
                    }
                    return true;
                case 199: // Home
                    this.setCursorToStart();
                    return true;
                case 207: // End
                    this.setCursorToEnd();
                    return true;
                default:
                    if (SharedConstants.isValidChar(typedChar)) {
                        if (this.editable) {
                            this.write(Character.toString(typedChar));
                        }
                        return true;
                    }
                    return false;
            }
        }
    }

    public int getInnerWidth() {
        return this.dim.width();
    }

    // Query and results access methods

    /**
     * Gets the current search query text
     */
    public String getQuery() {
        return this.text;
    }

    /**
     * Gets the last completed search query
     */
    public String getLastQuery() {
        return this.lastSearchRef != null ? this.lastSearchRef.get() : "";
    }

    /**
     * Sets the last search query
     */
    public void setLastQuery(String query) {
        if (this.lastSearchRef != null) {
            this.lastSearchRef.set(query);
        }
    }

    /**
     * Gets the current search result index
     */
    public int getLastSearchIndex() {
        return this.lastSearchIndexRef != null ? this.lastSearchIndexRef.get() : 0;
    }

    /**
     * Sets the search result index
     */
    public void setLastSearchIndex(int index) {
        if (this.lastSearchIndexRef != null) {
            this.lastSearchIndexRef.set(index);
        }
    }

    /**
     * Resets the search index to 0
     */
    public void resetSearchIndex() {
        if (this.lastSearchIndexRef != null) {
            this.lastSearchIndexRef.set(0);
        }
    }

    /**
     * Checks if there is an active search query
     */
    public boolean hasQuery() {
        return !this.text.trim().isEmpty();
    }

    /**
     * Clears the search field and resets state
     */
    public void clear() {
        this.setText("");
        if (this.lastSearchRef != null) {
            this.lastSearchRef.set("");
        }
        if (this.lastSearchIndexRef != null) {
            this.lastSearchIndexRef.set(0);
        }
    }
}