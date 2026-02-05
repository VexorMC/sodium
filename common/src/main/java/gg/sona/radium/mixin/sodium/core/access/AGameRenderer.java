package gg.sona.radium.mixin.sodium.core.access;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface AGameRenderer {
    @Invoker
    void invokeLoadShader(Identifier id);
}
