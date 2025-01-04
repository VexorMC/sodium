package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ColorVertex;
import net.caffeinemc.mods.sodium.client.render.immediate.CloudRenderer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.sodium.CloudVertex;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CloudRenderer.class)
public abstract class MixinCloudRenderer {
	@Unique
	private static final int[] NORMALS = new int[]{
		NormI8.pack(0.0f, -1.0f, 0.0f), // NEG_Y
		NormI8.pack(0.0f, 1.0f, 0.0f), // POS_Y
		NormI8.pack(-1.0f, 0.0f, 0.0f), //	NEG_X
		NormI8.pack(1.0f, 0.0f, 0.0f), // POS_X
		NormI8.pack(0.0f, 0.0f, -1.0f), // NEG_Z
		NormI8.pack(0.0f, 0.0f, 1.0f) // POS_Z
	};

	@Unique
	private static int computedNormal;

	@Shadow(remap = false)
	@Nullable
	private CloudRenderer.@Nullable CloudGeometry builtGeometry;

	@Unique
	private static boolean hadShadersOn = false;

	// Bitmasks for each cloud face
	private static final int FACE_MASK_NEG_Y = 1 << 0;
	private static final int FACE_MASK_POS_Y = 1 << 1;
	private static final int FACE_MASK_NEG_X = 1 << 2;
	private static final int FACE_MASK_POS_X = 1 << 3;
	private static final int FACE_MASK_NEG_Z = 1 << 4;
	private static final int FACE_MASK_POS_Z = 1 << 5;

	@Inject(method = "writeVertex", at = @At("HEAD"), cancellable = true, remap = false)
	private static void writeIrisVertex(long buffer, float x, float y, float z, int color, CallbackInfoReturnable<Long> cir) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			CloudVertex.put(buffer, x, y, z, color, computedNormal);
			cir.setReturnValue(buffer + 20L);
		}
	}

	@Inject(method = "emitCellGeometryFlat", at = @At("HEAD"), remap = false)
	private static void computeNormal2D(VertexBufferWriter writer, int texel, int x, int z, CallbackInfo ci) {
		computedNormal = NORMALS[0];
	}

	@Inject(method = "emitCellGeometryExterior", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/util/ColorABGR;mulRGB(II)I", ordinal = 0), remap = false)
	private static void computeNormal3D(VertexBufferWriter writer, int cellFaces, int cellColor, int cellX, int cellZ, CallbackInfo ci) {
		computedNormal = NORMALS[0];
	}

	@Inject(method = "emitCellGeometryExterior", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/util/ColorABGR;mulRGB(II)I", ordinal = 1), remap = false)
	private static void computeNormal3DUp(VertexBufferWriter writer, int cellFaces, int cellColor, int cellX, int cellZ, CallbackInfo ci) {
		computedNormal = NORMALS[1];
	}

	@Inject(method = "emitCellGeometryExterior", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;writeVertex(JFFFI)J", ordinal = 8, remap = false), remap = false)
	private static void computeNormal3DNegX(VertexBufferWriter writer, int cellFaces, int cellColor, int cellX, int cellZ, CallbackInfo ci) {
		computedNormal = NORMALS[2];
	}

	@Inject(remap = false, method = "emitCellGeometryExterior", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;writeVertex(JFFFI)J", ordinal = 12, remap = false))
	private static void computeNormal3DPosX(VertexBufferWriter writer, int cellFaces, int cellColor, int cellX, int cellZ, CallbackInfo ci) {
		computedNormal = NORMALS[3];
	}

	@Inject(method = "emitCellGeometryExterior", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;writeVertex(JFFFI)J", ordinal = 16), remap = false)
	private static void computeNormal3DNegZ(VertexBufferWriter writer, int cellFaces, int cellColor, int cellX, int cellZ, CallbackInfo ci) {
		computedNormal = NORMALS[4];
	}

	@Inject(method = "emitCellGeometryExterior", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;writeVertex(JFFFI)J", ordinal = 20), remap = false)
	private static void computeNormal3DPosZ(VertexBufferWriter writer, int cellFaces, int cellColor, int cellX, int cellZ, CallbackInfo ci) {
		computedNormal = NORMALS[5];
	}

	@ModifyArg(remap = false, method = "emitCellGeometryExterior", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"))
	private static int allocateNewSize(int size) {
		return IrisApi.getInstance().isShaderPackInUse() ? 480 : size;
	}

	@ModifyArg(remap = false, method = "emitCellGeometryInterior", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"))
	private static int allocateNewSizeInt(int size) {
		return IrisApi.getInstance().isShaderPackInUse() ? 480 : size;
	}

	@ModifyArg(method = "rebuildGeometry", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tesselator;begin(Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"), index = 1)
	private static VertexFormat rebuild(VertexFormat p_350837_) {
		return IrisApi.getInstance().isShaderPackInUse() ? IrisVertexFormats.CLOUDS : p_350837_;
	}

	@ModifyArg(method = "emitCellGeometryExterior", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;push(Lorg/lwjgl/system/MemoryStack;JILcom/mojang/blaze3d/vertex/VertexFormat;)V"), index = 3)
	private static VertexFormat modifyArgIris(VertexFormat vertexFormatDescription) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return IrisVertexFormats.CLOUDS;
		} else {
			return ColorVertex.FORMAT;
		}
	}

	@ModifyArg(method = "emitCellGeometryInterior", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;push(Lorg/lwjgl/system/MemoryStack;JILcom/mojang/blaze3d/vertex/VertexFormat;)V"), index = 3)
	private static VertexFormat modifyArgIrisInt(VertexFormat vertexFormatDescription) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return IrisVertexFormats.CLOUDS;
		} else {
			return ColorVertex.FORMAT;
		}
	}

	@ModifyArg(remap = false, method = "emitCellGeometryFlat", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"))
	private static int allocateNewSize2D(int size) {
		return IrisApi.getInstance().isShaderPackInUse() ? 80 : size;
	}

	@ModifyArg(method = "emitCellGeometryFlat", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;push(Lorg/lwjgl/system/MemoryStack;JILcom/mojang/blaze3d/vertex/VertexFormat;)V"), index = 3)
	private static VertexFormat modifyArgIris2D(VertexFormat vertexFormatDescription) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return IrisVertexFormats.CLOUDS;
		} else {
			return ColorVertex.FORMAT;
		}
	}

	@WrapOperation(method = "render", at = @At(remap = false, value = "INVOKE", target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z"))
	private boolean changeGeometry(Object a, Object b, Operation<Boolean> original) {
		return hadShadersOn == Iris.isPackInUseQuick() && original.call(a, b);
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V"), index = 2)
	private ShaderInstance iris$changeProgram(ShaderInstance p_253993_) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof ShaderRenderingPipeline) {
			return ((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(ShaderKey.CLOUDS_SODIUM);
		}

		return p_253993_;
	}

	@Inject(method = "rebuildGeometry", at = @At(remap = false, value = "HEAD"), remap = false)
	private static void changeGeometry2(CallbackInfoReturnable<CloudRenderer.CloudGeometry> cir) {
		hadShadersOn = IrisApi.getInstance().isShaderPackInUse();
	}

	// TODO interiors
}
