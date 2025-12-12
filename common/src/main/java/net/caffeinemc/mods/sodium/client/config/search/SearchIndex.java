package net.caffeinemc.mods.sodium.client.config.search;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageDefinition;

public abstract class SearchIndex {
    private final Runnable registerCallback;
    private LanguageDefinition builtLanguage;

    SearchIndex(Runnable registerCallback) {
        this.registerCallback = registerCallback;
    }

    public abstract void register(TextSource source);

    abstract void buildIndexInitial();

    abstract void rebuildIndex();

    protected abstract SearchQuerySession createQuery();

    public SearchQuerySession startQuery() {
        var currentLanguage = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        if (this.builtLanguage == null) {
            this.builtLanguage = currentLanguage;
            this.registerCallback.run();
            this.buildIndexInitial();
        } else if (this.builtLanguage != currentLanguage) {
            this.builtLanguage = currentLanguage;
            this.rebuildIndex();
        }

        return this.createQuery();
    }
}
