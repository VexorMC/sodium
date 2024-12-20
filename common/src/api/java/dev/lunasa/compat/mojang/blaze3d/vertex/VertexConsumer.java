package dev.lunasa.compat.mojang.blaze3d.vertex;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import dev.lunasa.compat.mojang.minecraft.FastColor;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Vec3i;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public interface VertexConsumer {
    public VertexConsumer addVertex(float var1, float var2, float var3);

    public VertexConsumer setColor(int var1, int var2, int var3, int var4);

    public VertexConsumer setUv(float var1, float var2);

    public VertexConsumer setUv1(int var1, int var2);

    public VertexConsumer setUv2(int var1, int var2);

    public VertexConsumer setNormal(float var1, float var2, float var3);

    default public void addVertex(float f, float f2, float f3, int n, float f4, float f5, int n2, int n3, float f6, float f7, float f8) {
        this.addVertex(f, f2, f3);
        this.setColor(n);
        this.setUv(f4, f5);
        this.setOverlay(n2);
        this.setLight(n3);
        this.setNormal(f6, f7, f8);
    }

    default public VertexConsumer setColor(float f, float f2, float f3, float f4) {
        return this.setColor((int)(f * 255.0f), (int)(f2 * 255.0f), (int)(f3 * 255.0f), (int)(f4 * 255.0f));
    }

    default public VertexConsumer setColor(int n) {
        return this.setColor(FastColor.ARGB32.red(n), FastColor.ARGB32.green(n), FastColor.ARGB32.blue(n), FastColor.ARGB32.alpha(n));
    }

    default public VertexConsumer setWhiteAlpha(int n) {
        return this.setColor(FastColor.ARGB32.color(n, -1));
    }

    default public VertexConsumer setLight(int n) {
        return this.setUv2(n & 0xFFFF, n >> 16 & 0xFFFF);
    }

    default public VertexConsumer setOverlay(int n) {
        return this.setUv1(n & 0xFFFF, n >> 16 & 0xFFFF);
    }

    default public void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float f, float f2, float f3, float f4, int n, int n2) {
        this.putBulkData(pose, bakedQuad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, f, f2, f3, f4, new int[]{n, n, n, n}, n2, false);
    }

    default public void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fArray, float f, float f2, float f3, float f4, int[] nArray, int n, boolean bl) {
        int[] nArray2 = bakedQuad.getVertexData();
        Vec3i vec3i = bakedQuad.getFace().getVector();
        Matrix4f matrix4f = pose.pose();
        Vector3f vector3f = pose.transformNormal(vec3i.getX(), vec3i.getY(), vec3i.getZ(), new Vector3f());
        int n2 = 8;
        int n3 = nArray2.length / 8;
        int n4 = (int) (f4 * 255.0f);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(VertexFormats.BLOCK.getVertexSize());
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        for (int i = 0; i < n3; ++i) {
            float f5;
            float f6;
            float f7;
            float f8;
            intBuffer.clear();
            intBuffer.put(nArray2, i * 8, 8);
            float f9 = byteBuffer.getFloat(0);
            float f10 = byteBuffer.getFloat(4);
            float f11 = byteBuffer.getFloat(8);
            if (bl) {
                float f12 = byteBuffer.get(12) & 0xFF;
                float f13 = byteBuffer.get(13) & 0xFF;
                f8 = byteBuffer.get(14) & 0xFF;
                f7 = f12 * fArray[i] * f;
                f6 = f13 * fArray[i] * f2;
                f5 = f8 * fArray[i] * f3;
            } else {
                f7 = fArray[i] * f * 255.0f;
                f6 = fArray[i] * f2 * 255.0f;
                f5 = fArray[i] * f3 * 255.0f;
            }
            int n5 = FastColor.ARGB32.color(n4, (int) f7, (int) f6, (int) f5);
            int n6 = nArray[i];
            f8 = byteBuffer.getFloat(16);
            float f14 = byteBuffer.getFloat(20);
            Vector3f vector3f2 = matrix4f.transformPosition(f9, f10, f11, new Vector3f());
            this.addVertex(vector3f2.x(), vector3f2.y(), vector3f2.z(), n5, f8, f14, n, n6, vector3f.x(), vector3f.y(), vector3f.z());
        }
    }

    default public VertexConsumer addVertex(Vector3f vector3f) {
        return this.addVertex(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer addVertex(PoseStack.Pose pose, Vector3f vector3f) {
        return this.addVertex(pose, vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer addVertex(PoseStack.Pose pose, float f, float f2, float f3) {
        return this.addVertex(pose.pose(), f, f2, f3);
    }

    default public VertexConsumer addVertex(Matrix4f matrix, float x, float y, float z) {
        float xt = MatrixHelper.transformPositionX(matrix, x, y, z);
        float yt = MatrixHelper.transformPositionY(matrix, x, y, z);
        float zt = MatrixHelper.transformPositionZ(matrix, x, y, z);

        return this.addVertex(xt, yt, zt);
    }

    default public VertexConsumer setNormal(PoseStack.Pose pose, float x, float y, float z) {

        Matrix3f matrix = pose.normal();

        float xt = MatrixHelper.transformNormalX(matrix, x, y, z);
        float yt = MatrixHelper.transformNormalY(matrix, x, y, z);
        float zt = MatrixHelper.transformNormalZ(matrix, x, y, z);

        if (!pose.trustedNormals) {
            float scalar = org.joml.Math.invsqrt(org.joml.Math.fma(xt, xt, Math.fma(yt, yt, zt * zt)));

            xt *= scalar;
            yt *= scalar;
            zt *= scalar;
        }

        return this.setNormal(xt, yt, zt);
    }
}