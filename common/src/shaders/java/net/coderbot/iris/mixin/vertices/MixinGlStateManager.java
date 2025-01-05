package net.coderbot.iris.mixin.vertices;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL20C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Fixes a copy-paste error relevant with generic vertex attributes
 */
@Mixin(GlStateManager.class)
public class MixinGlStateManager {

}
