package gg.sona.radium.mixin.core.mcpatcher;

import gg.sona.radium.mcpatcher.core.AbstractTextureExpansion;
import net.minecraft.client.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractTexture.class)
public class MixinAbstractTexture implements AbstractTextureExpansion {
    @Override
    public void unloadGLTexture() {
        ((AbstractTexture) (Object) this).clearGlId();
    }
}
