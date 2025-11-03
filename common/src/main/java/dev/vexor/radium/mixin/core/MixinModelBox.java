package dev.vexor.radium.mixin.core;

import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.render.immediate.model.EntityRenderer;
import net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.minecraft.client.render.ModelBox;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.model.ModelPart;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ModelBox.class)
public class MixinModelBox {

    @Mutable
    @Shadow @Final public float minX;
    @Shadow @Final public float minY;
    @Shadow @Final public float minZ;
    @Shadow @Final public float maxX;
    @Shadow @Final public float maxY;
    @Shadow @Final public float maxZ;

    @Unique
    private ModelCuboid sodium$cuboid;

    @Inject(
            method = "<init>(Lnet/minecraft/client/render/model/ModelPart;IIFFFIIIFZ)V",
            at = @At("TAIL")
    )
    private void init(ModelPart modelPart, int u, int v,
                        float x, float y, float z,
                        int sizeX, int sizeY, int sizeZ,
                        float extra, boolean mirror, CallbackInfo ci) {
        this.sodium$cuboid = new ModelCuboid(
                u, v,
                x, y, z,
                sizeX, sizeY, sizeZ,
                extra, extra, extra,
                mirror,
                modelPart.textureWidth,
                modelPart.textureHeight,
                Set.of(Direction.values())
        );
    }

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void draw(BufferBuilder builder, float scale, CallbackInfo ci) {
        VertexBufferWriter writer = VertexBufferWriter.of(builder);
        if (writer == null) {
            return;
        }

        ci.cancel();
        EntityRenderer.renderCuboid(writer, this.sodium$cuboid,
                /* light */ 0, // todo(entity-rendering): pass proper light value
                ColorARGB.toABGR(0xFFFFFFFF));
    }
}
