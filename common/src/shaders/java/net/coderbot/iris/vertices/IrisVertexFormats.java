package net.coderbot.iris.vertices;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public class IrisVertexFormats {
	public static final VertexFormatElement ENTITY_ELEMENT;
	public static final VertexFormatElement MID_TEXTURE_ELEMENT;
	public static final VertexFormatElement TANGENT_ELEMENT;
	public static final VertexFormatElement MID_BLOCK_ELEMENT;

	public static final VertexFormat TERRAIN;
	public static final VertexFormat ENTITY;

	static {
        TERRAIN = new VertexFormat();
        ENTITY = new VertexFormat();

        ENTITY_ELEMENT = new VertexFormatElement(11, VertexFormatElement.Format.SHORT, VertexFormatElement.Type.UV, 2);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(12, VertexFormatElement.Format.FLOAT, VertexFormatElement.Type.UV, 2);
		TANGENT_ELEMENT = new VertexFormatElement(13, VertexFormatElement.Format.BYTE, VertexFormatElement.Type.POSITION, 4);
		MID_BLOCK_ELEMENT = new VertexFormatElement(14, VertexFormatElement.Format.BYTE, VertexFormatElement.Type.NORMAL, 3);

		ImmutableList.Builder<VertexFormatElement> terrainElements = ImmutableList.builder();
		ImmutableList.Builder<VertexFormatElement> entityElements = ImmutableList.builder();

		terrainElements.add(VertexFormats.POSITION_ELEMENT); // 12
		terrainElements.add(VertexFormats.COLOR_ELEMENT); // 16
		terrainElements.add(VertexFormats.TEXTURE_FLOAT_ELEMENT); // 24
		terrainElements.add(VertexFormats.TEXTURE_SHORT_ELEMENT); // 28
		terrainElements.add(VertexFormats.NORMAL_ELEMENT); // 31
		terrainElements.add(VertexFormats.PADDING_ELEMENT); // 32
		terrainElements.add(ENTITY_ELEMENT); // 36
		terrainElements.add(MID_TEXTURE_ELEMENT); // 44
		terrainElements.add(TANGENT_ELEMENT); // 48
		terrainElements.add(MID_BLOCK_ELEMENT); // 51
		terrainElements.add(VertexFormats.PADDING_ELEMENT); // 52

		entityElements.add(VertexFormats.POSITION_ELEMENT); // 12
		entityElements.add(VertexFormats.COLOR_ELEMENT); // 16
		entityElements.add(VertexFormats.TEXTURE_FLOAT_ELEMENT); // 24
		entityElements.add(VertexFormats.TEXTURE_SHORT_ELEMENT); // 28
		entityElements.add(VertexFormats.NORMAL_ELEMENT); // 35
		entityElements.add(VertexFormats.PADDING_ELEMENT); // 36
		entityElements.add(MID_TEXTURE_ELEMENT); // 44
		entityElements.add(TANGENT_ELEMENT); // 48

        terrainElements.build().forEach(TERRAIN::addElement);
        entityElements.build().forEach(ENTITY::addElement);
	}
}
