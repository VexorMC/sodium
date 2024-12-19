package net.caffeinemc.mods.sodium.mixin.core.render.texture;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {
    @Accessor
    Map<ResourceLocation, Sprite> getTexturesByName();
}
