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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import dev.vexor.radium.frapi.api.renderer.v1.material.RenderMaterial;

public interface QuadView {
	/** Count of integers in a conventional (un-modded) block or item vertex. */
	int VANILLA_VERTEX_STRIDE = VertexFormats.POSITION_TEXTURE_COLOR_NORMAL.getVertexSize() / 4;

	/** Count of integers in a conventional (un-modded) block or item quad. */
	int VANILLA_QUAD_STRIDE = VANILLA_VERTEX_STRIDE * 4;

	/**
	 * Retrieve geometric position, x coordinate.
	 */
	float x(int vertexIndex);

	/**
	 * Retrieve geometric position, y coordinate.
	 */
	float y(int vertexIndex);

	/**
	 * Retrieve geometric position, z coordinate.
	 */
	float z(int vertexIndex);

	/**
	 * Convenience: access x, y, z by index 0-2.
	 */
	float posByIndex(int vertexIndex, int coordinateIndex);

	/**
	 * Retrieve vertex color in ARGB format (0xAARRGGBB).
	 */
	int color(int vertexIndex);

	/**
	 * Retrieve horizontal texture coordinates.
	 */
	float u(int vertexIndex);

	/**
	 * Retrieve vertical texture coordinates.
	 */
	float v(int vertexIndex);

	/**
	 * Minimum block brightness. Zero if not set.
	 */
	int lightmap(int vertexIndex);

	/**
	 * If false, no vertex normal was provided.
	 * Lighting should use face normal in that case.
	 */
	boolean hasNormal(int vertexIndex);

	/**
	 * If non-null, quad should not be rendered in-world if the
	 * opposite face of a neighbor block occludes it.
	 *
	 * @see MutableQuadView#cullFace(Direction)
	 */
	@Nullable
	Direction cullFace();

	/**
	 * Equivalent to {@link BakedQuad#getFace()}. This is the face used for vanilla lighting
	 * calculations and will be the block face to which the quad is most closely aligned. Always
	 * the same as cull face for quads that are on a block face, but never null.
	 */
	@NotNull
	Direction lightFace();

	/**
	 * See {@link MutableQuadView#nominalFace(Direction)}.
	 */
	@Nullable
	Direction nominalFace();

	/**
	 * Normal of the quad as implied by geometry. Will be invalid
	 * if quad vertices are not co-planar. Typically computed lazily
	 * on demand.
	 *
	 * <p>Not typically needed by models. Exposed to enable standard lighting
	 * utility functions for use by renderers.
	 */
	Vector3fc faceNormal();

	/**
	 * Retrieves the material serialized with the quad.
	 */
	RenderMaterial material();

	/**
	 * Retrieves the quad tint index serialized with the quad.
	 */
	int tintIndex();

	/**
	 * Retrieves the integer tag encoded with this quad via {@link MutableQuadView#tag(int)}.
	 * Will return zero if no tag was set. For use by models.
	 */
	int tag();
}
