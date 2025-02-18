package dev.vexor.radium.mixin.core;

import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Framebuffer.class)
public class MixinFramebuffer {
    @ModifyConstant(method = "attachTexture", constant = @Constant(intValue = 32856))
    public int radium$attachTexture(int constant) {
        return GL11.GL_RGBA16;
    }
}