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

package dev.vexor.radium.frapi.api.renderer.v1.mesh;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import dev.vexor.radium.frapi.api.renderer.v1.material.RenderMaterial;

/**
 * Specialized {@link MutableQuadView} that supports transformers and
 * sends quads to some destination, such as a mesh builder or rendering.
 *
 * <p>Instances of {@link QuadEmitter} will practically always be
 * thread local and/or reused - do not retain references.
 *
 * <p>Only the renderer should implement or extend this interface.
 */
public interface QuadEmitter extends MutableQuadView {
	@Override
	QuadEmitter pos(int vertexIndex, float x, float y, float z);

	@Override
	default QuadEmitter pos(int vertexIndex, Vector3f pos) {
		MutableQuadView.super.pos(vertexIndex, pos);
		return this;
	}

	@Override
	default QuadEmitter pos(int vertexIndex, Vector3fc pos) {
		MutableQuadView.super.pos(vertexIndex, pos);
		return this;
	}

	@Override
	QuadEmitter color(int vertexIndex, int color);

	@Override
	default QuadEmitter color(int c0, int c1, int c2, int c3) {
		MutableQuadView.super.color(c0, c1, c2, c3);
		return this;
	}

	@Override
	QuadEmitter uv(int vertexIndex, float u, float v);

	@Override
	default QuadEmitter uv(int vertexIndex, Vector2f uv) {
		MutableQuadView.super.uv(vertexIndex, uv);
		return this;
	}

	@Override
	default QuadEmitter uv(int vertexIndex, Vector2fc uv) {
		MutableQuadView.super.uv(vertexIndex, uv);
		return this;
	}

	@Override
	QuadEmitter spriteBake(Sprite sprite, int bakeFlags);

	@Override
	QuadEmitter lightmap(int vertexIndex, int lightmap);

	@Override
	default QuadEmitter lightmap(int b0, int b1, int b2, int b3) {
		MutableQuadView.super.lightmap(b0, b1, b2, b3);
		return this;
	}

	@Override
	QuadEmitter normal(int vertexIndex, float x, float y, float z);

	@Override
	default QuadEmitter normal(int vertexIndex, Vector3f normal) {
		MutableQuadView.super.normal(vertexIndex, normal);
		return this;
	}

	@Override
	default QuadEmitter normal(int vertexIndex, Vector3fc normal) {
		MutableQuadView.super.normal(vertexIndex, normal);
		return this;
	}

	@Override
	QuadEmitter cullFace(@Nullable Direction face);

	@Override
	QuadEmitter nominalFace(@Nullable Direction face);

	@Override
	QuadEmitter material(RenderMaterial material);

	@Override
	QuadEmitter tintIndex(int tintIndex);

	@Override
	QuadEmitter tag(int tag);

	@Override
	QuadEmitter fromVanilla(int[] quadData, int startIndex);

	@Override
	QuadEmitter fromVanilla(BakedQuad quad, RenderMaterial material, @Nullable Direction cullFace);

	/**
	 * In static mesh building, causes quad to be appended to the mesh being built. In a dynamic render context, create
	 * a new quad to be output to rendering. In both cases, current instance is reset to default values.
	 */
	QuadEmitter emit();
}
