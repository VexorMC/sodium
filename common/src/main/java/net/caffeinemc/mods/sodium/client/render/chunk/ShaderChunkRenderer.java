package net.caffeinemc.mods.sodium.client.render.chunk;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.*;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.gl.shader.*;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class ShaderChunkRenderer implements ChunkRenderer {
    private final Map<ChunkShaderOptions, GlProgram<ChunkShaderInterface>> programs = new Object2ObjectOpenHashMap<>();

    protected final ChunkVertexType vertexType;
    protected final GlVertexFormat vertexFormat;

    protected final RenderDevice device;

    protected GlProgram<ChunkShaderInterface> activeProgram;

    public ShaderChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
        this.device = device;
        this.vertexType = vertexType;
        this.vertexFormat = vertexType.getVertexFormat();
    }

    protected GlProgram<ChunkShaderInterface> compileProgram(ChunkShaderOptions options) {
        GlProgram<ChunkShaderInterface> program = this.programs.get(options);

        if (program == null) {
            this.programs.put(options, program = this.createShader("blocks/block_layer_opaque", options));
        }

        return program;
    }

    private GlProgram<ChunkShaderInterface> createShader(String path, ChunkShaderOptions options) {
        ShaderConstants constants = options.constants();

        GlShader vertShader = ShaderLoader.loadShader(ShaderType.VERTEX,
                new Identifier("sodium", path + ".vsh"), constants);

        GlShader fragShader = ShaderLoader.loadShader(ShaderType.FRAGMENT,
                new Identifier("sodium", path + ".fsh"), constants);

        try {
            return GlProgram.builder(new Identifier("sodium", "chunk_shader"))
                    .attachShader(vertShader)
                    .attachShader(fragShader)
                    .bindAttribute("a_Position", ChunkShaderBindingPoints.ATTRIBUTE_POSITION)
                    .bindAttribute("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR)
                    .bindAttribute("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_TEXTURE)
                    .bindAttribute("a_LightAndData", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_MATERIAL_INDEX)
                    .bindFragmentData("fragColor", ChunkShaderBindingPoints.FRAG_COLOR)
                    .link((shader) -> new DefaultShaderInterface(shader, options));
        } finally {
            vertShader.delete();
            fragShader.delete();
        }
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

    protected void begin(TerrainRenderPass pass) {
        pass.startDrawing();

        ChunkShaderOptions options = new ChunkShaderOptions(ChunkFogMode.SMOOTH, pass, this.vertexType);

        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        GlProgram<ChunkShaderInterface> program = null;


        if (program == null) {
            program = this.compileProgram(options);
        }

        this.activeProgram = program;
        this.activeProgram.bind();
        this.activeProgram.getInterface()
                .setupState();
    }

    protected void end(TerrainRenderPass pass) {
        this.activeProgram.getInterface()
                .resetState();
        this.activeProgram.unbind();
        this.activeProgram = null;

        pass.endDrawing();
    }

    @Override
    public void delete(CommandList commandList) {
        this.programs.values()
                .forEach(GlProgram::delete);
    }


}
