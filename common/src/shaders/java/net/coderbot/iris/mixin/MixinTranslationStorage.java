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

@Mixin(TranslationStorage.class)
public class MixinTranslationStorage {
    @Shadow
    Map<String, String> translations;

    @Unique
    private static final ArrayList<String> codes = new ArrayList<>();

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

        for (String code : codes) {
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
    private void iris$addLanguageCodes(ResourceManager resourceManager, List<String> languages, CallbackInfo ci) {
        codes.clear();

        // Reverse order due to how minecraft has English and then the primary language in the language definitions list
        new LinkedList<>(languages).descendingIterator().forEachRemaining(codes::add);
    }

}
