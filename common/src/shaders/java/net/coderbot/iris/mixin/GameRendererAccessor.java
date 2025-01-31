package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor
    int getFrameCount();
    @Accessor
    void setFrameCount(int frameCount);
}
