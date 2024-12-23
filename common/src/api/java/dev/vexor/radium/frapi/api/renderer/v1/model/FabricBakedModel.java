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

package dev.vexor.radium.frapi.api.renderer.v1.model;

import java.util.function.Predicate;
import java.util.function.Supplier;

import dev.vexor.radium.compat.mojang.minecraft.random.RandomSource;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import dev.vexor.radium.frapi.api.renderer.v1.mesh.MutableQuadView;
import dev.vexor.radium.frapi.api.renderer.v1.mesh.QuadEmitter;
import dev.vexor.radium.frapi.impl.renderer.VanillaModelEncoder;

/**
 * Interface for baked models that output meshes with enhanced rendering features.
 * Can also be used to generate or customize outputs based on world state instead of
 * or in addition to block state when render chunks are rebuilt.
 *
 * <p>Implementors should have a look at {@link ModelHelper} as it contains many useful functions.
 *
 * <p>Note: This interface is automatically implemented on all baked models via Mixin and interface injection.
 */
public interface FabricBakedModel {
	/**
	 * When true, signals renderer this producer is implemented through
	 * {@link BakedModel#getQuads()}.
	 * Also means the model does not rely on any non-vanilla features.
	 * Allows the renderer to optimize or route vanilla models through the unmodified vanilla pipeline if desired.
	 *
	 * <p>Vanilla baked models will return true.
	 * Enhanced models that use this API should return false,
	 * otherwise the API will not recognize the model.
	 */
	default boolean isVanillaAdapter() {
		return true;
	}

	/**
	 * This method will be called during chunk rebuilds to generate both the static and
	 * dynamic portions of a block model when the model implements this interface and
	 * {@link #isVanillaAdapter()} returns false.
	 *
	 * <p>During chunk rebuild, this method will always be called exactly one time per block
	 * position, irrespective of which or how many faces or block render layers are included
	 * in the model. Models must output all quads in a single pass.
	 *
	 * <p>Also called to render block models outside of chunk rebuild or block entity rendering.
	 * Typically, this happens when the block is being rendered as an entity, not as a block placed in the world.
	 * Currently, this happens for falling blocks and blocks being pushed by a piston, but renderers
	 * should invoke this for all calls to
	 * {@link BlockModelRenderer#render(BlockView, BakedModel, BlockState, BlockPos, BufferBuilder, boolean)}
	 * that occur outside of chunk rebuilds to allow for features added by mods, unless {@link #isVanillaAdapter()}
	 * returns true.
	 *
	 * <p>Outside of chunk rebuilds, this method will be called every frame. Model implementations should
	 * rely on pre-baked meshes as much as possible and keep transformation to a minimum.  The provided
	 * block position may be the <em>nearest</em> block position and not actual. For this reason, neighbor
	 * state lookups are best avoided or will require special handling. Block entity lookups are
	 * likely to fail and/or give meaningless results.
	 *
	 * <p>Note: with {@link BakedModel#getQuads()}, the random
	 * parameter is normally initialized with the same seed prior to each face layer.
	 * Model authors should note this method is called only once per block, and call the provided
	 * Random supplier multiple times if re-seeding is necessary.
	 *
	 * @param emitter Accepts model output.
	 * @param blockView Access to world state.
	 * @param state Block state for model being rendered.
	 * @param pos Position of block for model being rendered.
	 * @param randomSupplier Random object seeded per vanilla conventions. Call multiple times to re-seed.
	 *                       Will not be thread-safe. Do not cache or retain a reference.
	 * @param cullTest A test that returns {@code true} for faces which will be culled and {@code false} for faces which
	 *                 may or may not be culled. Meant to be used to cull groups of quads or expensive dynamic quads
	 *                 early for performance. Early culled quads will likely not be added the emitter, so callers of
	 *                 this method must account for this. In general, prefer using
	 *                 {@link MutableQuadView#cullFace(Direction)} instead of this test.
	 */
	default void emitBlockQuads(QuadEmitter emitter, BlockView blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
		VanillaModelEncoder.emitBlockQuads(emitter, (BakedModel) this, state, randomSupplier, cullTest);
	}
}
