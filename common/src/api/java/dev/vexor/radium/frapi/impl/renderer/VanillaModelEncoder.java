/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.vexor.radium.frapi.impl.renderer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import dev.vexor.radium.compat.mojang.minecraft.random.RandomSource;
import net.legacyfabric.fabric.api.util.TriState;
import net.minecraft.block.*;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import dev.vexor.radium.frapi.api.renderer.v1.Renderer;
import dev.vexor.radium.frapi.api.renderer.v1.material.RenderMaterial;
import dev.vexor.radium.frapi.api.renderer.v1.material.ShadeMode;
import dev.vexor.radium.frapi.api.renderer.v1.mesh.QuadEmitter;
import dev.vexor.radium.frapi.api.renderer.v1.model.ModelHelper;

/**
 * Routines for adaptation of vanilla {@link BakedModel}s to FRAPI pipelines.
 */
public class VanillaModelEncoder {
	private static final RenderMaterial STANDARD_MATERIAL = Renderer.get().materialFinder().shadeMode(ShadeMode.VANILLA).find();
	private static final RenderMaterial NO_AO_MATERIAL = Renderer.get().materialFinder().shadeMode(ShadeMode.VANILLA).ambientOcclusion(TriState.FALSE).find();

    public static void emitBlockQuads(QuadEmitter emitter, BakedModel model, @Nullable BlockState state, Predicate<@Nullable Direction> cullTest) {
        final RenderMaterial defaultMaterial = model.useAmbientOcclusion() ? STANDARD_MATERIAL : NO_AO_MATERIAL;

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            final Direction cullFace = ModelHelper.faceFromIndex(i);

            if (cullTest.test(cullFace)) {
                continue;
            }

            final List<BakedQuad> quads;

            if (cullFace != null) {
                quads = model.getByDirection(cullFace);
            } else {
                quads = model.getQuads();
            }

            Set<Direction> allowedFaces = Arrays.stream(Direction.values()).collect(Collectors.toSet());

            for (Class<?> clazz : FILTER) {
                if (clazz.isInstance(state.getBlock())) {
                    allowedFaces = findAllowedFaces(quads);
                    break;
                }
            }

            if (cullFace != null) {
                for (final BakedQuad quad : quads) {
                    emitter.fromVanilla(quad, defaultMaterial, cullFace);
                    emitter.emit();
                }
            } else {
                for (final BakedQuad quad : quads) {
                    if (allowedFaces.contains(quad.getFace())) {
                        emitter.fromVanilla(quad, defaultMaterial, cullFace);
                        emitter.emit();
                    }
                }
            }
        }
    }

    /**
     * Finds allowed faces from a list of baked quads
     *
     * @param quads quads to find the allowed faces from
     * @return the allowed faces
     */
    private static Set<Direction> findAllowedFaces(List<BakedQuad> quads) {
        Set<Direction> allowedFaces = new HashSet<>();

        for (BakedQuad quad : quads) {
            Direction faceDirection = quad.getFace();

            if (faceDirection == Direction.NORTH || faceDirection == Direction.SOUTH) {
                if (!allowedFaces.contains(Direction.NORTH)) {
                    allowedFaces.add(Direction.NORTH);
                }
            } else if (faceDirection == Direction.WEST || faceDirection == Direction.EAST) {
                if (!allowedFaces.contains(Direction.EAST)) {
                    allowedFaces.add(Direction.EAST);
                }
            } else {
                allowedFaces.add(faceDirection);
            }
        }

        return allowedFaces;
    }

    private static final Class<?>[] FILTER = { PlantBlock.class, Growable.class, GrassBlock.class };
}
