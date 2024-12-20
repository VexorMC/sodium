package net.caffeinemc.mods.sodium.mixin.core.render;

import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.fabricmc.fabric.impl.renderer.SpriteFinderImpl;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(SpriteAtlasTexture.class)
public class TextureAtlasMixin implements SpriteFinderImpl.SpriteFinderAccess {
    @Shadow
    @Final
    public Map<String, Sprite> sprites;

    @Inject(method = "update", at = @At("RETURN"))
    private void sodium$deleteSpriteFinder(CallbackInfo ci) {
        SpriteFinderCache.resetSpriteFinder();
    }

    @Override
    public SpriteFinderImpl fabric$spriteFinder() {
        Map<Identifier, Sprite> sprites = new HashMap<>();

        this.sprites.forEach((id, sprite) -> {
            sprites.put(new Identifier(id), sprite);
        });

        return new SpriteFinderImpl(
                sprites,
                (SpriteAtlasTexture) (Object) this
        );
    }
}