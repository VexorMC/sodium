package net.fabricmc.fabric.impl.renderer;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;

import dev.lunasa.compat.mojang.minecraft.random.RandomSource;
import net.legacyfabric.fabric.api.util.TriState;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;

/**
 * Routines for adaptation of vanilla {@link BakedModel}s to FRAPI pipelines.
 */
public class VanillaModelEncoder {
    private static final RenderMaterial STANDARD_MATERIAL = Renderer.get().materialFinder().shadeMode(ShadeMode.VANILLA).find();
    private static final RenderMaterial NO_AO_MATERIAL = Renderer.get().materialFinder().shadeMode(ShadeMode.VANILLA).ambientOcclusion(TriState.FALSE).find();

    public static void emitBlockQuads(QuadEmitter emitter, BakedModel model, @Nullable BlockState state, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
        final RenderMaterial defaultMaterial = model.useAmbientOcclusion() ? STANDARD_MATERIAL : NO_AO_MATERIAL;

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            final Direction cullFace = ModelHelper.faceFromIndex(i);

            if (cullTest.test(cullFace)) {
                // Skip entire quad list if possible.
                continue;
            }

            final List<BakedQuad> quads = model.getByDirection(cullFace);
            quads.addAll(model.getQuads());
            final int count = quads.size();

            for (int j = 0; j < count; j++) {
                final BakedQuad q = quads.get(j);
                emitter.fromVanilla(q, defaultMaterial, cullFace);
                emitter.emit();
            }
        }
    }

    public static void emitItemQuads(QuadEmitter emitter, BakedModel model, @Nullable BlockState state, Supplier<Random> randomSupplier) {
        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            final Direction cullFace = ModelHelper.faceFromIndex(i);
            final List<BakedQuad> quads = model.getByDirection(cullFace);
            quads.addAll(model.getQuads());
            final int count = quads.size();

            for (int j = 0; j < count; j++) {
                final BakedQuad q = quads.get(j);
                emitter.fromVanilla(q, STANDARD_MATERIAL, cullFace);
                emitter.emit();
            }
        }
    }
}
