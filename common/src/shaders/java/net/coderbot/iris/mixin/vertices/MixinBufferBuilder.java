package net.coderbot.iris.mixin.vertices;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.coderbot.iris.vertices.BufferBuilderPolygonView;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.coderbot.iris.vertices.ExtendingBufferBuilder;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormalHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

/**
 * Dynamically and transparently extends the vanilla vertex formats with additional data
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements BlockSensitiveBufferBuilder, ExtendingBufferBuilder {
	@Unique
	private boolean extending;

	@Unique
	private boolean iris$shouldNotExtend = false;

	@Unique
	private boolean iris$isTerrain = false;

	@Unique
	private int vertexCount;

	@Unique
	private final BufferBuilderPolygonView polygon = new BufferBuilderPolygonView();

	@Unique
	private final Vector3f normal = new Vector3f();

	@Unique
	private boolean injectNormal;

	@Unique
	private short currentBlock;

	@Unique
	private short currentRenderType;

	@Unique
	private int currentLocalPosX;

	@Unique
	private int currentLocalPosY;

	@Unique
	private int currentLocalPosZ;


	@Shadow
	private ByteBuffer buffer;

	@Shadow
	private int drawMode;

	@Shadow
	private VertexFormat format;

	@Shadow
	private int currentElementId;

	@Shadow
	private @Nullable VertexFormatElement currentElement;

	@Shadow
	public abstract void begin(int drawMode, VertexFormat vertexFormat);

    @Shadow
    protected abstract void nextElement();

    @Override
	public void iris$beginWithoutExtending(int drawMode, VertexFormat vertexFormat) {
		iris$shouldNotExtend = true;
		begin(drawMode, vertexFormat);
		iris$shouldNotExtend = false;
	}

	@Inject(method = "begin", at = @At("HEAD"))
	private void iris$onBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
		boolean shouldExtend = (!iris$shouldNotExtend) && BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat();
		extending = shouldExtend && (format == VertexFormats.BLOCK || format == VertexFormats.ENTITY
			|| format == VertexFormats.POSITION_TEXTURE_COLOR);
		vertexCount = 0;

		if (extending) {
			injectNormal = format == VertexFormats.POSITION_TEXTURE_COLOR;
		}
	}

	@Inject(method = "begin", at = @At("RETURN"))
	private void iris$afterBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
		if (extending) {
			if (format == VertexFormats.ENTITY) {
				this.format = IrisVertexFormats.ENTITY;
				this.iris$isTerrain = false;
			} else {
				this.format = IrisVertexFormats.TERRAIN;
				this.iris$isTerrain = true;
			}
			this.currentElement = this.format.getElements().get(0);
		}
	}

	@Inject(method = "reset", at = @At("HEAD"))
	private void iris$onReset(CallbackInfo ci) {
		extending = false;
		vertexCount = 0;
	}

	@Inject(method = "begin", at = @At("RETURN"))
	private void iris$preventHardcodedVertexWriting(int drawMode, VertexFormat format, CallbackInfo ci) {
		if (!extending) {
			return;
		}
	}

	@Inject(method = "next", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (!extending) {
			return;
		}

		if (injectNormal && currentElement == VertexFormats.NORMAL_ELEMENT) {
			this.putInt(0, 0);
			this.nextElement();
		}

		if (iris$isTerrain) {
			// ENTITY_ELEMENT
			this.putShort(0, currentBlock);
			this.putShort(2, currentRenderType);
			this.nextElement();
		}
		// MID_TEXTURE_ELEMENT
		this.putFloat(0, 0);
		this.putFloat(4, 0);
		this.nextElement();
		// TANGENT_ELEMENT
		this.putInt(0, 0);
		this.nextElement();
		if (iris$isTerrain) {
			// MID_BLOCK_ELEMENT
			int posIndex = this.currentElementId - 48;
			float x = buffer.getFloat(posIndex);
			float y = buffer.getFloat(posIndex + 4);
			float z = buffer.getFloat(posIndex + 8);
			this.putInt(0, ExtendedDataHelper.computeMidBlock(x, y, z, currentLocalPosX, currentLocalPosY, currentLocalPosZ));
			this.nextElement();
		}

		vertexCount++;

		if (drawMode == GL11.GL_QUADS && vertexCount == 4 || drawMode == GL11.GL_TRIANGLES && vertexCount == 3) {
			fillExtendedData(vertexCount);
		}
	}

	@Unique
	private void fillExtendedData(int vertexAmount) {
		vertexCount = 0;

		int stride = format.getVertexSize();

		polygon.setup(buffer, currentElementId, stride, vertexAmount);

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < vertexAmount; vertex++) {
			midU += polygon.u(vertex);
			midV += polygon.v(vertex);
		}

		midU /= vertexAmount;
		midV /= vertexAmount;

		if (vertexAmount == 3) {
			NormalHelper.computeFaceNormalTri(normal, polygon);
		} else {
			NormalHelper.computeFaceNormal(normal, polygon);
		}
		int packedNormal = NormalHelper.packNormal(normal, 0.0f);

		int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, polygon);

		int midUOffset;
		int midVOffset;
		int normalOffset;
		int tangentOffset;
		if (iris$isTerrain) {
			midUOffset = 16;
			midVOffset = 12;
			normalOffset = 24;
			tangentOffset = 8;
		} else {
			midUOffset = 12;
			midVOffset = 8;
			normalOffset = 16;
			tangentOffset = 4;
		}

		for (int vertex = 0; vertex < vertexAmount; vertex++) {
			buffer.putFloat(currentElementId - midUOffset - stride * vertex, midU);
			buffer.putFloat(currentElementId - midVOffset - stride * vertex, midV);
			buffer.putInt(currentElementId - normalOffset - stride * vertex, packedNormal);
			buffer.putInt(currentElementId - tangentOffset - stride * vertex, tangent);
		}
	}

	@Override
	public void beginBlock(short block, short renderType, int localPosX, int localPosY, int localPosZ) {
		this.currentBlock = block;
		this.currentRenderType = renderType;
		this.currentLocalPosX = localPosX;
		this.currentLocalPosY = localPosY;
		this.currentLocalPosZ = localPosZ;
	}

	@Override
	public void endBlock() {
		this.currentBlock = -1;
		this.currentRenderType = -1;
		this.currentLocalPosX = 0;
		this.currentLocalPosY = 0;
		this.currentLocalPosZ = 0;
	}

    @Unique
    private void putInt(int i, int value) {
        this.buffer.putInt(this.currentElementId + i, value);
    }
    @Unique
    private void putShort(int i, short value) {
        this.buffer.putShort(this.currentElementId + i, value);
    }
    @Unique
    private void putFloat(int i, float value) {
        this.buffer.putFloat(this.currentElementId + i, value);
    }
}
