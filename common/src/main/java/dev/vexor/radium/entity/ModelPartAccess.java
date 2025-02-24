package dev.vexor.radium.entity;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.render.BufferBuilder;

public interface ModelPartAccess {
    void radium$render(PoseStack stack, BufferBuilder buffer, float scale);
}
