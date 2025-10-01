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

package net.caffeinemc.mods.sodium.client.render.frapi.mesh;


import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFlags;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.GeometryHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.NormalHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.material.RenderMaterialImpl;
import dev.vexor.radium.frapi.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.caffeinemc.mods.sodium.client.render.frapi.mesh.EncodingFormat.*;

/**
 * Base class for all quads / quad makers. Handles the ugly bits
 * of maintaining and encoding the quad state.
 */
public class QuadViewImpl implements QuadView, ModelQuadView {
    @Nullable
    protected Direction nominalFace;
    /** True when face normal, light face, normal face, or geometry flags may not match geometry. */
    protected boolean isGeometryInvalid = true;
    protected final Vector3f faceNormal = new Vector3f();

    /** Size and where it comes from will vary in subtypes. But in all cases quad is fully encoded to array. */
    protected int[] data;

    /** Beginning of the quad. Also, the header index. */
    protected int baseIndex = 0;

    /**
     * Decodes necessary state from the backing data array.
     * The encoded data must contain valid computed geometry.
     */
    public void load() {
        isGeometryInvalid = false;
        nominalFace = lightFace();
        NormI8.unpack(packedFaceNormal(), faceNormal);
    }

    protected void computeGeometry() {
        if (isGeometryInvalid) {
            isGeometryInvalid = false;

            NormalHelper.computeFaceNormal(faceNormal, this);
            int packedFaceNormal = NormI8.pack(faceNormal);
            data[baseIndex + HEADER_FACE_NORMAL] = packedFaceNormal;

            // depends on face normal
            Direction lightFace = GeometryHelper.lightFace(this);
            data[baseIndex + HEADER_BITS] = EncodingFormat.lightFace(data[baseIndex + HEADER_BITS], lightFace);

            // depends on face normal
            data[baseIndex + HEADER_BITS] = EncodingFormat.normalFace(data[baseIndex + HEADER_BITS], ModelQuadFacing.fromPackedNormal(packedFaceNormal));

            // depends on light face
            data[baseIndex + HEADER_BITS] = EncodingFormat.geometryFlags(data[baseIndex + HEADER_BITS], ModelQuadFlags.getQuadFlags(this, lightFace));
        }
    }

    /** gets flags used for lighting - lazily computed via {@link ModelQuadFlags#getQuadFlags}. */
    public int geometryFlags() {
        computeGeometry();
        return EncodingFormat.geometryFlags(data[baseIndex + HEADER_BITS]);
    }

    public boolean hasShade() {
        return !material().disableDiffuse();
    }

    @Override
    public float x(int vertexIndex) {
        return Float.intBitsToFloat(data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_X]);
    }

    @Override
    public float y(int vertexIndex) {
        return Float.intBitsToFloat(data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_Y]);
    }

    @Override
    public float z(int vertexIndex) {
        return Float.intBitsToFloat(data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_Z]);
    }

    @Override
    public float posByIndex(int vertexIndex, int coordinateIndex) {
        return Float.intBitsToFloat(data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_X + coordinateIndex]);
    }

    @Override
    public int color(int vertexIndex) {
        return data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_COLOR];
    }

    @Override
    public float u(int vertexIndex) {
        return Float.intBitsToFloat(data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_U]);
    }

    @Override
    public float v(int vertexIndex) {
        return Float.intBitsToFloat(data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_V]);
    }

    @Override
    public int lightmap(int vertexIndex) {
        return data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_LIGHTMAP];
    }

    public int normalFlags() {
        return EncodingFormat.normalFlags(data[baseIndex + HEADER_BITS]);
    }

    @Override
    public boolean hasNormal(int vertexIndex) {
        return (normalFlags() & (1 << vertexIndex)) != 0;
    }

    protected final int normalIndex(int vertexIndex) {
        return baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_NORMAL;
    }

    @Override
    @Nullable
    public final Direction cullFace() {
        return EncodingFormat.cullFace(data[baseIndex + HEADER_BITS]);
    }

    @Override
    @NotNull
    public final Direction lightFace() {
        computeGeometry();
        return EncodingFormat.lightFace(data[baseIndex + HEADER_BITS]);
    }

    public final ModelQuadFacing normalFace() {
        computeGeometry();
        return EncodingFormat.normalFace(data[baseIndex + HEADER_BITS]);
    }

    @Override
    @Nullable
    public final Direction nominalFace() {
        return nominalFace;
    }

    public final int packedFaceNormal() {
        computeGeometry();
        return data[baseIndex + HEADER_FACE_NORMAL];
    }

    @Override
    public final Vector3f faceNormal() {
        computeGeometry();
        return faceNormal;
    }

    @Override
    public final RenderMaterialImpl material() {
        return EncodingFormat.material(data[baseIndex + HEADER_BITS]);
    }

    @Override
    public final int tintIndex() {
        return data[baseIndex + HEADER_TINT_INDEX];
    }

    @Override
    public final int tag() {
        return data[baseIndex + HEADER_TAG];
    }

    // ModelQuadView method implementations below

    @Override
    public float getX(int idx) {
        return x(idx);
    }

    @Override
    public float getY(int idx) {
        return y(idx);
    }

    @Override
    public float getZ(int idx) {
        return z(idx);
    }

    @Override
    public int getColor(int idx) {
        return ColorHelper.toVanillaColor(color(idx));
    }

    @Override
    public float getTexU(int idx) {
        return u(idx);
    }

    @Override
    public float getTexV(int idx) {
        return v(idx);
    }

    @Override
    public int getVertexNormal(int idx) {
        return data[normalIndex(idx - 1)];
    }

    @Override
    public int getFaceNormal() {
        return packedFaceNormal();
    }

    @Override
    public int getLight(int idx) {
        return lightmap(idx);
    }

    @Override
    public int getTintIndex() {
        return tintIndex();
    }

    @Override
    public Sprite getSprite() {
        throw new UnsupportedOperationException("Not available for QuadViewImpl.");
    }

    @Override
    public Direction getLightFace() {
        return lightFace();
    }

    @Override
    public int getMaxLightQuad(int idx) {
        return lightmap(idx);
    }

    @Override
    public int getFlags() {
        return geometryFlags();
    }
}
