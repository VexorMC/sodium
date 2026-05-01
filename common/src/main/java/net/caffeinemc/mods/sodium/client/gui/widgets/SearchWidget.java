package net.caffeinemc.mods.sodium.client.gui.widgets;


import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.search.SearchQuerySession;
import net.caffeinemc.mods.sodium.client.config.structure.Option;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.util.Dim2i;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SearchWidget extends AbstractParentWidget {
    // maximum distance from its original position that a search result can be moved to improve grouping
    private static final int MAX_ORDER_DIST_ERROR = 2;

    private final Consumer<List<Option.OptionNameSource>> onSearchResults;
    private final SearchQuerySession searchQuerySession;
    private final AtomicReference<String> lastSearchRef;
    private final AtomicReference<Integer> lastSearchIndexRef;
    private String query = "";
    private SearchTextFieldWidget searchBox;
    private FlatButtonWidget clearButton;
    private int lastRebuildWidth = -1;

    /**
     * Simple constructor without external state references
     */
    public SearchWidget(Consumer<List<Option.OptionNameSource>> onSearchResults, Dim2i dim) {
        this(onSearchResults, dim, new AtomicReference<>(""), new AtomicReference<>(0));
    }

    /**
     * Full constructor with external state references for persistence
     */
    public SearchWidget(Consumer<List<Option.OptionNameSource>> onSearchResults, Dim2i dim,
                        AtomicReference<String> lastSearch, AtomicReference<Integer> lastSearchIndex) {
        super(dim);
        this.onSearchResults = onSearchResults;
        this.searchQuerySession = ConfigManager.CONFIG.startSearchQuery();
        this.lastSearchRef = lastSearch;
        this.lastSearchIndexRef = lastSearchIndex;
    }

    public void updateWidgetWidth(int width) {
        if (width != this.lastRebuildWidth) {
            this.lastRebuildWidth = width;
            this.rebuildForWidth(width);
        }
    }

    private void rebuildForWidth(int width) {
        this.clearChildren();

        int x = this.getX();
        int y = this.getY();

        int searchBoxWidth = width - Layout.BUTTON_SHORT;

        this.clearButton = new FlatButtonWidget(
                new Dim2i(x + searchBoxWidth, y, Layout.BUTTON_SHORT, Layout.BUTTON_SHORT),
                new LiteralText("x"),
                this::clearSearch,
                true,
                false
        );

        // Create custom search text field with state references
        int textFieldX = x + Layout.INNER_MARGIN;
        int textFieldY = y + Layout.BUTTON_SHORT / 2 - this.font.fontHeight / 2;
        int textFieldWidth = searchBoxWidth - 20;

        this.searchBox = new SearchTextFieldWidget(
                new Dim2i(textFieldX, textFieldY, textFieldWidth, Layout.BUTTON_SHORT),
                this::triggerSearch,
                new TranslatableText("sodium.options.search.hint"),
                this.lastSearchRef,
                this.lastSearchIndexRef
        );

        this.searchBox.setMaxLength(200);

        this.addChild(this.searchBox);
        this.addChild(this.clearButton);
    }

    private void clearSearch() {
        this.searchBox.clear();
        this.query = "";
        this.search();
        this.setFocused(null);
    }

    private void triggerSearch(String text) {
        if (text.equals(this.query)) {
            return;
        }

        this.query = text.stripLeading();
        this.search();
    }

    @SuppressWarnings("unchecked") // we manually check the elements
    private void search() {
        var results = this.searchQuerySession.getSearchResults(this.query);

        // assert assumption of the result type
        for (int i = 0; i < results.size(); i++) {
            var result = results.get(i);
            result.setResultIndex(i);

            if (!(result instanceof Option.OptionNameSource)) {
                throw new UnsupportedOperationException("Unsupported search text source type: " + result.getClass().getName());
            }
        }

        List<Option.OptionNameSource> typedResults = (List<Option.OptionNameSource>) results;

        this.improveGrouping(typedResults);
        this.onSearchResults.accept(typedResults);
    }

    private void improveGrouping(List<Option.OptionNameSource> searchResults) {
        // move search results around a little to group them better
        var length = searchResults.size();
        for (int i = 1; i < length - 1; i++) {
            // if the next result would fit better to the previous one than this one, swap current and next
            var prev = searchResults.get(i - 1);
            var curr = searchResults.get(i);
            var next = searchResults.get(i + 1);

            // check that switching current and next doesn't introduce too much of an ordering error
            if (Math.abs(i - prev.getResultIndex()) > MAX_ORDER_DIST_ERROR ||
                    Math.abs(i + 1 - next.getResultIndex()) > MAX_ORDER_DIST_ERROR) {
                continue;
            }

            var prevCurrScore = this.getGroupScore(prev, curr);
            var prevNextScore = this.getGroupScore(prev, next);

            if (prevNextScore > prevCurrScore) {
                searchResults.set(i, next);
                searchResults.set(i + 1, curr);
            }
        }
    }

    private int getGroupScore(Option.OptionNameSource a, Option.OptionNameSource b) {
        if (a.getModOptions() != b.getModOptions()) {
            return 0;
        }
        if (a.getPage() != b.getPage()) {
            return 1;
        }
        if (a.getOptionGroup() != b.getOptionGroup()) {
            return 2;
        }
        return 3;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        // Draw background for search box area
        drawRect(

                this.getX(),
                this.getY(),
                this.getX() + this.lastRebuildWidth - Layout.BUTTON_SHORT,
                this.getLimitY(),
                Colors.BACKGROUND_DEFAULT
        );

        // Render search box and clear button
        this.searchBox.render(mouseX, mouseY, delta);
        this.clearButton.render(mouseX, mouseY, delta);

        super.render(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, char typedChar) {
        if (this.searchBox.isFocused()) {
            return this.searchBox.keyPressed(keyCode, typedChar);
        }
        return super.keyPressed(keyCode, typedChar);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Let search box handle clicks first
        if (this.searchBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean isSearching() {
        return this.searchBox.isFocused();
    }

    /**
     * Gets the current search query
     */
    public String getQuery() {
        return this.searchBox != null ? this.searchBox.getQuery() : "";
    }

    /**
     * Gets the last completed search query
     */
    public String getLastQuery() {
        return this.searchBox != null ? this.searchBox.getLastQuery() : "";
    }

    /**
     * Gets the current search result index
     */
    public int getSearchResultIndex() {
        return this.searchBox != null ? this.searchBox.getLastSearchIndex() : 0;
    }

    /**
     * Sets the search result index
     */
    public void setSearchResultIndex(int index) {
        if (this.searchBox != null) {
            this.searchBox.setLastSearchIndex(index);
        }
    }

    /**
     * Checks if there is an active search query
     */
    public boolean hasQuery() {
        return this.searchBox != null && this.searchBox.hasQuery();
    }

    /**
     * Gets the search text field widget for direct access
     */
    public SearchTextFieldWidget getSearchBox() {
        return this.searchBox;
    }

    /**
     * Gets the last search AtomicReference for external state management
     */
    public AtomicReference<String> getLastSearchRef() {
        return this.lastSearchRef;
    }

    /**
     * Gets the last search index AtomicReference for external state management
     */
    public AtomicReference<Integer> getLastSearchIndexRef() {
        return this.lastSearchIndexRef;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);

        // on focus, focus the search box for typing
        if (focused) {
            this.setFocused(this.searchBox);
        }
    }
}
