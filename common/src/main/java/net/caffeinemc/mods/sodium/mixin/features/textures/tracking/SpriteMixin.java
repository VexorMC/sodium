package net.caffeinemc.mods.sodium.mixin.features.textures.tracking;

import net.caffeinemc.mods.sodium.client.render.texture.SpriteExtension;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Sprite.class)
public class SpriteMixin implements SpriteExtension {
    @Unique private boolean active = false;

    @Override
    public void sodium$setActive(boolean value) {
        this.active = value;
    }

    @Override
    public boolean sodium$isActive() {
        return this.active;
    }
}
