package net.caffeinemc.mods.sodium.client.render.immediate.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.util.Int2;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid.*;

public class EntityRenderer {
    private static final int VERTEX_BUFFER_BYTES =
            NUM_CUBE_FACES * NUM_FACE_VERTICES * 32; // 32 bytes per vertex (pos + uv + normal + padding)

    private static final long[] CUBE_VERTEX_XY = new long[NUM_CUBE_VERTICES];
    private static final long[] CUBE_VERTEX_ZW = new long[NUM_CUBE_VERTICES];
    private static final int[] CUBE_FACE_NORMAL = new int[NUM_CUBE_FACES];

    private static final FloatBuffer MODELVIEW = MemoryUtil.memAllocFloat(16);
    private static final FloatBuffer NORMALMAT = MemoryUtil.memAllocFloat(9);

    private static final float[] modelView = new float[16];
    private static final float[] normalMat = new float[9];

    private static final float[] tmpVec = new float[3];

    public static void renderCuboid(VertexBufferWriter writer, ModelCuboid cuboid, int light, int color) {
        updateMatrices();
        prepareVertices(cuboid, color);
        prepareNormals();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            long vertexBuffer = stack.nmalloc(64, VERTEX_BUFFER_BYTES);
            int vertexCount = emitQuads(vertexBuffer, cuboid, light);

            if (vertexCount > 0) {
                writer.push(stack, vertexBuffer, vertexCount, VertexFormats.ENTITY);
            }
        }
    }

    private static void updateMatrices() {
        GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW);
        MODELVIEW.get(modelView);
        MODELVIEW.rewind();

        normalMat[0] = modelView[0];
        normalMat[1] = modelView[1];
        normalMat[2] = modelView[2];
        normalMat[3] = modelView[4];
        normalMat[4] = modelView[5];
        normalMat[5] = modelView[6];
        normalMat[6] = modelView[8];
        normalMat[7] = modelView[9];
        normalMat[8] = modelView[10];
    }

    private static int emitQuads(long buffer, ModelCuboid cuboid, int light) {
        long ptr = buffer;
        int vertexCount = 0;

        int[] positions = cuboid.positions;
        long[] textures = cuboid.textures;
        int[] normals = cuboid.normals;

        for (int faceIndex = 0; faceIndex < NUM_CUBE_FACES; faceIndex++) {
            if (!cuboid.shouldDrawFace(faceIndex)) continue;

            int normal = CUBE_FACE_NORMAL[normals[faceIndex]];

            int offset = faceIndex * NUM_FACE_VERTICES;
            ptr = writeVertex(ptr, positions[offset], textures[offset], normal);
            ptr = writeVertex(ptr, positions[offset + 1], textures[offset + 1], normal);
            ptr = writeVertex(ptr, positions[offset + 2], textures[offset + 2], normal);
            ptr = writeVertex(ptr, positions[offset + 3], textures[offset + 3], normal);

            vertexCount += 4;
        }

        return vertexCount;
    }

    private static long writeVertex(long ptr, int vertexIndex, long packedUv, int packedNormal) {
        MemoryUtil.memPutLong(ptr + 0L, CUBE_VERTEX_XY[vertexIndex]); // x,y
        MemoryUtil.memPutLong(ptr + 8L, CUBE_VERTEX_ZW[vertexIndex]); // z,color (or unused)
        MemoryUtil.memPutLong(ptr + 16L, packedUv);                   // texture coords
        MemoryUtil.memPutInt(ptr + 24L, packedNormal);                // packed normal
        MemoryUtil.memPutInt(ptr + 28L, 0);                           // padding
        return ptr + 32L;
    }

    private static void prepareVertices(ModelCuboid cuboid, int color) {
        // transform base origin
        float[] o = transformPosition(cuboid.originX, cuboid.originY, cuboid.originZ);

        float[] vx = transformDirection(cuboid.sizeX, 0, 0);
        float[] vy = transformDirection(0, cuboid.sizeY, 0);
        float[] vz = transformDirection(0, 0, cuboid.sizeZ);

        // Build 8 cube vertices
        float[][] v = new float[8][3];
        v[VERTEX_X0_Y0_Z0] = new float[]{o[0], o[1], o[2]};
        v[VERTEX_X1_Y0_Z0] = add(o, vx);
        v[VERTEX_X1_Y1_Z0] = add(v[VERTEX_X1_Y0_Z0], vy);
        v[VERTEX_X0_Y1_Z0] = add(o, vy);
        v[VERTEX_X0_Y0_Z1] = add(o, vz);
        v[VERTEX_X1_Y0_Z1] = add(v[VERTEX_X1_Y0_Z0], vz);
        v[VERTEX_X1_Y1_Z1] = add(v[VERTEX_X1_Y1_Z0], vz);
        v[VERTEX_X0_Y1_Z1] = add(v[VERTEX_X0_Y1_Z0], vz);

        for (int i = 0; i < NUM_CUBE_VERTICES; i++) {
            setVertex(i, v[i][0], v[i][1], v[i][2], color);
        }
    }

    private static void setVertex(int index, float x, float y, float z, int color) {
        CUBE_VERTEX_XY[index] = Int2.pack(Float.floatToRawIntBits(x), Float.floatToRawIntBits(y));
        CUBE_VERTEX_ZW[index] = Int2.pack(Float.floatToRawIntBits(z), color);
    }

    private static float[] transformPosition(float x, float y, float z) {
        float[] m = modelView;
        tmpVec[0] = m[0] * x + m[4] * y + m[8] * z + m[12];
        tmpVec[1] = m[1] * x + m[5] * y + m[9] * z + m[13];
        tmpVec[2] = m[2] * x + m[6] * y + m[10] * z + m[14];
        return tmpVec.clone();
    }

    private static float[] transformDirection(float x, float y, float z) {
        float[] m = modelView;
        tmpVec[0] = m[0] * x + m[4] * y + m[8] * z;
        tmpVec[1] = m[1] * x + m[5] * y + m[9] * z;
        tmpVec[2] = m[2] * x + m[6] * y + m[10] * z;
        return tmpVec.clone();
    }

    private static float[] add(float[] a, float[] b) {
        return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private static void prepareNormals() {
        CUBE_FACE_NORMAL[FACE_NEG_Y] = packNormal(transformNormal(0, -1, 0));
        CUBE_FACE_NORMAL[FACE_POS_Y] = packNormal(transformNormal(0, 1, 0));
        CUBE_FACE_NORMAL[FACE_NEG_Z] = packNormal(transformNormal(0, 0, -1));
        CUBE_FACE_NORMAL[FACE_POS_Z] = packNormal(transformNormal(0, 0, 1));
        CUBE_FACE_NORMAL[FACE_POS_X] = packNormal(transformNormal(1, 0, 0));
        CUBE_FACE_NORMAL[FACE_NEG_X] = packNormal(transformNormal(-1, 0, 0));
    }

    private static float[] transformNormal(float x, float y, float z) {
        float[] m = normalMat;
        tmpVec[0] = m[0] * x + m[3] * y + m[6] * z;
        tmpVec[1] = m[1] * x + m[4] * y + m[7] * z;
        tmpVec[2] = m[2] * x + m[5] * y + m[8] * z;
        float len = MathHelper.sqrt(tmpVec[0]*tmpVec[0] + tmpVec[1]*tmpVec[1] + tmpVec[2]*tmpVec[2]);
        tmpVec[0] /= len;
        tmpVec[1] /= len;
        tmpVec[2] /= len;
        return tmpVec.clone();
    }

    private static int packNormal(float[] n) {
        int nx = (int) (n[0] * 127.0f) & 0xFF;
        int ny = (int) (n[1] * 127.0f) & 0xFF;
        int nz = (int) (n[2] * 127.0f) & 0xFF;
        return nx | (ny << 8) | (nz << 16);
    }
}
