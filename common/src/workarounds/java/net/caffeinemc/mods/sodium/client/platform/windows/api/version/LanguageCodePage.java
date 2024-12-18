package net.caffeinemc.mods.sodium.client.platform.windows.api.version;

import dev.lunasa.compat.lwjgl3.MemoryUtil;

public record LanguageCodePage(int languageId, int codePage) {
    static final int STRIDE = Integer.BYTES;

    static LanguageCodePage decode(long address) {
        var value = MemoryUtil.memGetInt(address);

        int languageId = value & 0xFFFF;
        int codePage = (value & 0xFFFF0000) >> 16;

        return new LanguageCodePage(languageId, codePage);
    }
}