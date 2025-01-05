package net.coderbot.iris.texture.pbr.loader;

import net.minecraft.client.resource.AnimationMetadata;
import net.minecraft.util.Identifier;

public class SpriteInfo {
    private final Identifier name;
    private final int width, height;
    private final AnimationMetadata metadata;

    public SpriteInfo(Identifier name, int width, int height, AnimationMetadata metadata) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.metadata = metadata;
    }

    public Identifier name() {
        return name;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public AnimationMetadata metadata() {
        return metadata;
    }
}
