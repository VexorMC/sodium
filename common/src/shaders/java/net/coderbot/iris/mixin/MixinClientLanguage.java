package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.LanguageMap;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Allows shader packs to provide extra usable language entries outside of resource packs.
 *
 * <p>We "sideload" the language entries with an override system to avoid having to reload the
 * resource manager on shader pack changes, since reloading the resource manager is very slow.</p>
 *
 * Uses a lower priority to inject before Incubus-Core to prevent translations from breaking
 * @see <a href="https://github.com/devs-immortal/Incubus-Core/blob/4edfff0f088bc1b7ea77a1d475f76801a03179a4/src/main/java/net/id/incubus_core/mixin/devel/client/TranslationStorageMixin.java">Incubus-Core translation mixin</a>
 */
@Mixin(value = TranslationStorage.class, priority = 990)
public class MixinClientLanguage {
    // storage
    @Shadow Map<String, String> translations;

    @Unique
    private static final List<String> languageCodes = new ArrayList<>();

    @Inject(method = "translate", at = @At("HEAD"), cancellable = true)
    private void iris$overrideLanguageEntries(String key, CallbackInfoReturnable<String> cir) {
        final String override = iris$lookupOverriddenEntry(key);

        if (override != null) {
            cir.setReturnValue(override);
        }
    }

    @Unique
    private String iris$lookupOverriddenEntry(String key) {
        final ShaderPack pack = Iris.getCurrentPack().orElse(null);

        if (pack == null) {
            // If no shaderpack is loaded, do not try to process language overrides.
            //
            // This prevents a cryptic NullPointerException when shaderpack loading fails for some reason.
            return null;
        }

        // Minecraft loads the "en_US" language code by default, and any other code will be right after it.
        //
        // So we also check if the user is loading a special language, and if the shaderpack has support for that
        // language. If they do, we load that, but if they do not, we load "en_US" instead.
        final LanguageMap languageMap = pack.getLanguageMap();

        if (translations.containsKey(key)) {
            return null;
        }

        for (String code : languageCodes) {
            final Map<String, String> translations = languageMap.getTranslations(code);

            if (translations != null) {
                final String translation = translations.get(key);

                if (translation != null) {
                    return translation;
                }
            }
        }

        return null;
    }

    @Inject(method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)V", at = @At("HEAD"))
    private void iris$addLanguageCodes(ResourceManager resourceManager, List<String> definitions, CallbackInfo ci) {
        languageCodes.clear();

        // Reverse order due to how minecraft has English and then the primary language in the language definitions list
        new LinkedList<>(definitions).descendingIterator().forEachRemaining(languageCodes::add);
    }
}
