package net.caffeinemc.mods.sodium.mixin.features.textures.animations.tracking;

import net.caffeinemc.mods.sodium.client.render.texture.SpriteContentsExtension;
import net.minecraft.client.resource.AnimationMetadata;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Sprite.class)
public abstract class SpriteContentsMixin implements SpriteContentsExtension {
    @Shadow
    private AnimationMetadata meta;
    @Unique
    private boolean active;

    @Override
    public void sodium$setActive(boolean value) {
        this.active = value;
    }

    @Override
    public boolean sodium$hasAnimation() {
        return this.meta != null;
    }

    @Override
    public boolean sodium$isActive() {
        return this.active;
    }
}
