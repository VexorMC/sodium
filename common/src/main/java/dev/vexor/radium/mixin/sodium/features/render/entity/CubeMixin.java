package dev.vexor.radium.mixin.sodium.features.render.entity;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.PoseStack;
import dev.vexor.radium.compat.mojang.blaze3d.vertex.VertexConsumer;
import dev.vexor.radium.compat.mojang.minecraft.render.LightTexture;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.render.immediate.EntityRenderer;
import net.caffeinemc.mods.sodium.client.render.immediate.ModelCuboid;
import net.caffeinemc.mods.sodium.client.render.vertex.VertexConsumerUtils;
import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.ModelBox;
import net.minecraft.client.render.model.ModelPart;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ModelBox.class)
public class CubeMixin {
    @Mutable
    @Shadow
    @Final
    public float minX;

    @Unique
    private ModelCuboid sodium$cuboid;

    // Inject at the start of the function, so we don't capture modified locals
    @Redirect(method = "<init>(Lnet/minecraft/client/render/model/ModelPart;IIFFFIIIFZ)V", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/render/ModelBox;minX:F", ordinal = 0))
    private void onInit(ModelBox instance, float value, ModelPart modelPart, int i, int j, float f, float g, float h, int k, int l, int m, float n, boolean bl) {
        this.sodium$cuboid = new ModelCuboid(i, j, f, g, h, k, l, m, n, n, n, bl, modelPart.textureWidth, modelPart.textureHeight, Set.of(DirectionUtil.ALL_DIRECTIONS));

        this.minX = value;
    }

    @Inject(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/TexturedQuad;draw(Lnet/minecraft/client/render/BufferBuilder;F)V"), cancellable = true)
    private void onCompile(BufferBuilder builder, float scale, CallbackInfo ci) {
        VertexBufferWriter writer = VertexConsumerUtils.convertOrLog((VertexConsumer) builder);

        if (writer == null) {
            return;
        }

        ci.cancel();

        float[] modelViewArray = new float[16];

        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewArray);

        Matrix4f modelViewMatrix = new Matrix4f().set(modelViewArray);

        Matrix3f normalMatrix = new Matrix3f();
        modelViewMatrix.get3x3(normalMatrix).invert().transpose();

        var pose = new PoseStack.Pose(modelViewMatrix, normalMatrix);

        EntityRenderer.renderCuboid(pose, writer, this.sodium$cuboid, LightTexture.FULL_BRIGHT, 0, ColorARGB.toABGR(-1));
    }
}