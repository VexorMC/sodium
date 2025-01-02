package net.irisshaders.iris.gl.program;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.GlResource;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import org.joml.Vector2f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

public final class ComputeProgram extends GlResource {
	private final ProgramUniforms uniforms;
	private final ProgramSamplers samplers;
	private final ProgramImages images;
	private final int[] localSize;
	private Vector3i absoluteWorkGroups;
	private Vector2f relativeWorkGroups;
	private float cachedWidth;
	private float cachedHeight;
	private Vector3i cachedWorkGroups;
	private FilledIndirectPointer indirectPointer;

	ComputeProgram(int program, ProgramUniforms uniforms, ProgramSamplers samplers, ProgramImages images) {
		super(program);

		localSize = new int[3];
		IrisRenderSystem.getProgramiv(program, GL43.GL_COMPUTE_WORK_GROUP_SIZE, localSize);
		this.uniforms = uniforms;
		this.samplers = samplers;
		this.images = images;
	}

	public static void unbind() {
		ProgramUniforms.clearActiveUniforms();
		GL20.glUseProgram(0);
	}

	public void setWorkGroupInfo(Vector2f relativeWorkGroups, Vector3i absoluteWorkGroups, FilledIndirectPointer indirectPointer) {
		this.relativeWorkGroups = relativeWorkGroups;
		this.absoluteWorkGroups = absoluteWorkGroups;
		this.indirectPointer = indirectPointer;
	}

	public Vector3i getWorkGroups(float width, float height) {
		if (indirectPointer != null) return null;

		if (cachedWidth != width || cachedHeight != height || cachedWorkGroups == null) {
			this.cachedWidth = width;
			this.cachedHeight = height;
			if (this.absoluteWorkGroups != null) {
				this.cachedWorkGroups = this.absoluteWorkGroups;
			} else if (relativeWorkGroups != null) {
				// Do not use actual localSize here, apparently that's not what we want.
				this.cachedWorkGroups = new Vector3i((int) Math.ceil(Math.ceil((width * relativeWorkGroups.x)) / localSize[0]), (int) Math.ceil(Math.ceil((height * relativeWorkGroups.y)) / localSize[1]), 1);
			} else {
				this.cachedWorkGroups = new Vector3i((int) Math.ceil(width / localSize[0]), (int) Math.ceil(height / localSize[1]), 1);
			}
		}

		return cachedWorkGroups;
	}

	public void use() {
        GL20.glUseProgram(getGlId());

		uniforms.update();
		samplers.update();
		images.update();
	}

	public void dispatch(float width, float height) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::allowConcurrentCompute).orElse(false)) {
			IrisRenderSystem.memoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL42.GL_TEXTURE_FETCH_BARRIER_BIT | GL43.GL_SHADER_STORAGE_BARRIER_BIT);
		}

		if (indirectPointer != null) {
			IrisRenderSystem.bindBuffer(GL43.GL_DISPATCH_INDIRECT_BUFFER, indirectPointer.buffer());
			IrisRenderSystem.dispatchComputeIndirect(indirectPointer.offset());
		} else {
			IrisRenderSystem.dispatchCompute(getWorkGroups(width, height));
		}
	}

	public void destroyInternal() {
		GL20.glDeleteProgram(getGlId());
	}

	/**
	 * @return the OpenGL ID of this program.
	 * @deprecated this should be encapsulated eventually
	 */
	@Deprecated
	public int getProgramId() {
		return getGlId();
	}

	public int getActiveImages() {
		return images.getActiveImages();
	}
}
