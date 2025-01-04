package net.irisshaders.iris.pathways;

import dev.vexor.radium.compat.mojang.Util;
import net.irisshaders.iris.layer.InnerWrappedRenderType;
import net.irisshaders.iris.layer.LightningRenderStateShard;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.VertexFormatMode;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class LightningHandler extends RenderType {
	public static final RenderType IRIS_LIGHTNING = new InnerWrappedRenderType("iris_lightning2", RenderType.create(
		"iris_lightning",
        VertexFormats.POSITION_COLOR,
		VertexFormatMode.QUADS,
		256,
		false,
		true,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setTransparencyState(LIGHTNING_TRANSPARENCY)
			.createCompositeState(false)
	), new LightningRenderStateShard());

	public LightningHandler(String pRenderType0, VertexFormat pVertexFormat1, VertexFormatMode pVertexFormat$Mode2, int pInt3, boolean pBoolean4, boolean pBoolean5, Runnable pRunnable6, Runnable pRunnable7) {
		super(pRenderType0, pVertexFormat1, pVertexFormat$Mode2, pInt3, pBoolean4, pBoolean5, pRunnable6, pRunnable7);
	}
}
