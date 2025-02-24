package dev.vexor.radium.entity;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.render.BufferBuilder;

public interface ModelBoxAccess {
    void radium$compile(PoseStack.Pose pose, BufferBuilder builder);
}
