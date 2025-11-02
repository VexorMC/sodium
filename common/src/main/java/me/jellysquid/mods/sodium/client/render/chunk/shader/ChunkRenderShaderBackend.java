package me.jellysquid.mods.sodium.client.render.chunk.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.gl.shader.*;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.compat.FogHelper;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderBackend;
import me.jellysquid.mods.sodium.client.render.chunk.format.ChunkMeshAttribute;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;

public abstract class ChunkRenderShaderBackend<T extends ChunkGraphicsState>
        implements ChunkRenderBackend<T> {
    private IrisChunkProgramOverrides irisChunkProgramOverrides;
    private RenderDevice device;
    private ChunkProgram override;

    private final EnumMap<ChunkFogMode, ChunkProgram> programs = new EnumMap<>(ChunkFogMode.class);

    protected final ChunkVertexType vertexType;
    protected final GlVertexFormat<ChunkMeshAttribute> vertexFormat;

    protected ChunkProgram activeProgram;

    public ChunkRenderShaderBackend(ChunkVertexType vertexType) {
        this.vertexType = vertexType;
        this.vertexFormat = vertexType.getCustomVertexFormat();
        this.irisChunkProgramOverrides = new IrisChunkProgramOverrides();
    }

    private GlShader loadShader(RenderDevice device, ShaderType type, Identifier name, List<String> constants) {
        if (this.vertexType == IrisModelVertexFormats.MODEL_VERTEX_XHFP) {
            String shader = getShaderSource(getShaderPath(name));
            shader = shader.replace("v_LightCoord = a_LightCoord", "v_LightCoord = (iris_LightmapTextureMatrix * vec4(a_LightCoord, 0, 1)).xy");

            StringTransformations transformations = new StringTransformations(shader);

            transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "mat4 iris_LightmapTextureMatrix = mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0));");

            return new GlShader(device, type, name, transformations.toString(), ShaderConstants.fromStringList(constants));
        } else {
            return ShaderLoader.loadShader(device, type, name, constants);
        }
    }

    private ChunkProgram createShader(RenderDevice device, ChunkFogMode fogMode, GlVertexFormat<ChunkMeshAttribute> vertexFormat) {
        GlShader vertShader = loadShader(device, ShaderType.VERTEX,
                new Identifier("sodium", "chunk_gl20.v.glsl"), fogMode.getDefines());

        GlShader fragShader = loadShader(device, ShaderType.FRAGMENT,
                new Identifier("sodium", "chunk_gl20.f.glsl"), fogMode.getDefines());

        try {
            return GlProgram.builder(new Identifier("sodium", "chunk_shader"))
                    .attachShader(vertShader)
                    .attachShader(fragShader)
                    .bindAttribute("a_Pos", ChunkShaderBindingPoints.POSITION)
                    .bindAttribute("a_Color", ChunkShaderBindingPoints.COLOR)
                    .bindAttribute("a_TexCoord", ChunkShaderBindingPoints.TEX_COORD)
                    .bindAttribute("a_LightCoord", ChunkShaderBindingPoints.LIGHT_COORD)
                    .bindAttribute("d_ModelOffset", ChunkShaderBindingPoints.MODEL_OFFSET)
                    .build((program, name) -> new ChunkProgram(device, program, name, fogMode.getFactory()));
        } finally {
            vertShader.delete();
            fragShader.delete();
        }
    }

    @Override
    public final void createShaders(RenderDevice device) {
        this.device = device;
        WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();
        SodiumTerrainPipeline sodiumTerrainPipeline = null;

        if (worldRenderingPipeline != null) {
            sodiumTerrainPipeline = worldRenderingPipeline.getSodiumTerrainPipeline();
        }

        irisChunkProgramOverrides.createShaders(sodiumTerrainPipeline, device);
        this.programs.put(ChunkFogMode.NONE, this.createShader(device, ChunkFogMode.NONE, this.vertexFormat));
        this.programs.put(ChunkFogMode.LINEAR, this.createShader(device, ChunkFogMode.LINEAR, this.vertexFormat));
        this.programs.put(ChunkFogMode.EXP2, this.createShader(device, ChunkFogMode.EXP2, this.vertexFormat));
    }

    @Override
    public void iris$begin(BlockRenderPass pass) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            // No back face culling during the shadow pass
            // TODO: Hopefully this won't be necessary in the future...
            RenderSystem.disableCull();
        }

        this.override = irisChunkProgramOverrides.getProgramOverride(device, pass);

        Iris.getPipelineManager().getPipeline().ifPresent(WorldRenderingPipeline::beginSodiumTerrainRendering);
        begin();
    }

    @Override
    public void begin() {
        this.activeProgram = this.programs.get(FogHelper.getFogMode());
        if (override != null) {
            this.activeProgram = override;
        }
        this.activeProgram.bind();
        this.activeProgram.setup(this.vertexType.getModelScale(), this.vertexType.getTextureScale());
    }

    @Override
    public void end() {
        this.activeProgram.unbind();
        this.activeProgram = null;
        ProgramUniforms.clearActiveUniforms();
        ProgramSamplers.clearActiveSamplers();
        Iris.getPipelineManager().getPipeline().ifPresent(WorldRenderingPipeline::endSodiumTerrainRendering);
    }

    @Override
    public void delete() {
        for (ChunkProgram shader : this.programs.values()) {
            shader.delete();
        }
        irisChunkProgramOverrides.deleteShaders();
    }

    @Override
    public ChunkVertexType getVertexType() {
        return this.vertexType;
    }


    private static String getShaderPath(Identifier name) {
        return String.format("/assets/%s/shaders/%s", name.getNamespace(), name.getPath());
    }

    private static String getShaderSource(String path) {
        try {
            InputStream in = ShaderLoader.class.getResourceAsStream(path);
            Throwable var2 = null;

            String var3;
            try {
                if (in == null) {
                    throw new RuntimeException("Shader not found: " + path);
                }

                var3 = IOUtils.toString(in, StandardCharsets.UTF_8);
            } catch (Throwable var13) {
                var2 = var13;
                throw var13;
            } finally {
                if (in != null) {
                    if (var2 != null) {
                        try {
                            in.close();
                        } catch (Throwable var12) {
                            var2.addSuppressed(var12);
                        }
                    } else {
                        in.close();
                    }
                }

            }

            return var3;
        } catch (IOException var15) {
            throw new RuntimeException("Could not read shader sources", var15);
        }
    }
}
