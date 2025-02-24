package dev.vexor.radium.mixin.sodium.features.render.entity;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.PoseStack;
import dev.vexor.radium.entity.ModelBoxAccess;
import dev.vexor.radium.entity.ModelPartAccess;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.ModelBox;
import net.minecraft.client.render.model.ModelPart;
import net.minecraft.client.util.GlAllocationUtils;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements ModelPartAccess {
    @Shadow
    public boolean hide;

    @Shadow
    public boolean visible;

    @Shadow
    public float offsetX;

    @Shadow
    public float offsetY;

    @Shadow
    public float offsetZ;

    @Shadow
    public float pivotX;

    @Shadow
    public float pivotY;

    @Shadow
    public float pivotZ;

    // rotationX, in radians
    @Shadow
    public float posX;

    // rotationY, in radians
    @Shadow
    public float posY;

    // rotationZ, in radians
    @Shadow
    public float posZ;

    @Shadow
    private boolean compiledList;

    @Shadow
    private int glList;

    @Shadow
    public List<ModelPart> modelList;

    @Shadow
    public List<ModelBox> cuboids;

    @Unique
    private boolean radium$hasRotation() {
        return posX != 0 && posY != 0 && posZ != 0;
    }

    @Unique
    private void radium$translateAndRotate(PoseStack stack, float scale) {
        stack.translate(this.offsetX + this.pivotX, this.offsetY + this.pivotY, this.offsetZ + this.pivotZ);

        if (radium$hasRotation()) {
            stack.mulPose((new Quaternionf()).rotationZYX(this.posX, this.posY, this.posZ));
        }

        if (scale != 1.0f) {
            stack.scale(scale, scale, scale);
        }
    }

    @Override
    public void radium$render(PoseStack stack, BufferBuilder buffer, float scale) {
        if (!this.hide && this.visible && (this.modelList != null && !this.modelList.isEmpty())) {
            stack.pushPose();
            radium$translateAndRotate(stack, scale);
            if (!this.compiledList) {
                radium$compile(stack.last(), buffer);
            }
            for (ModelPart modelPart : this.modelList) {
                var access = (ModelPartAccess) modelPart;
                access.radium$render(stack, buffer, scale);
            }
            stack.popPose();
        }
    }

    @Unique
    private void radium$compile(PoseStack.Pose pose, BufferBuilder buffer) {
        this.glList = GlAllocationUtils.genLists(1);
        GL11.glNewList(this.glList, GL11.GL_COMPILE);

        for (ModelBox cuboid : this.cuboids) {
            var access = (ModelBoxAccess) cuboid;
            access.radium$compile(pose, buffer);
        }

        GL11.glEndList();
        this.compiledList = true;
    }
}
