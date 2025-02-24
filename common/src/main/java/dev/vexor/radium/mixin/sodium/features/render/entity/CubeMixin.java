package dev.vexor.radium.mixin.sodium.features.render.entity;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.PoseStack;
import dev.vexor.radium.compat.mojang.blaze3d.vertex.VertexConsumer;
import dev.vexor.radium.entity.ModelBoxAccess;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.render.immediate.EntityRenderer;
import net.caffeinemc.mods.sodium.client.render.immediate.ModelCuboid;
import net.caffeinemc.mods.sodium.client.render.vertex.VertexConsumerUtils;
import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.ModelPart;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

import static org.lwjgl.opengl.GL11C.GL_QUADS;

@Mixin(ModelBox.class)
public class CubeMixin implements ModelBoxAccess {
    @Unique
    private ModelCuboid sodium$cuboid;

    @Redirect(method = "<init>(Lnet/minecraft/client/render/model/ModelPart;IIFFFIIIFZ)V", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/render/ModelBox;minX:F", ordinal = 0))
    private void onInit(ModelBox instance, float value, ModelPart modelPart, int u, int v, float x1, float y1, float z1, int dx, int dy, int dz, float delta, boolean mirrored) {
        this.sodium$cuboid = new ModelCuboid(u, v, x1, y1, z1, dx, dy, dz, delta, delta, delta, mirrored, modelPart.textureWidth, modelPart.textureHeight, Set.of(DirectionUtil.ALL_DIRECTIONS));
    }

    @Override
    public void radium$compile(PoseStack.Pose pose, BufferBuilder builder) {
        VertexBufferWriter writer = VertexConsumerUtils.convertOrLog((VertexConsumer) builder);

        if (writer == null) {
            return;
        }

        builder.begin(GL_QUADS, VertexFormats.ENTITY);

        EntityRenderer.renderCuboid(pose, writer, this.sodium$cuboid);
        Tessellator.getInstance().draw();
    }
}