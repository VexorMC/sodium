package net.irisshaders.iris.vertices;

import net.irisshaders.iris.Iris;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public class IrisVertexFormats {
	public static final VertexFormatElement ENTITY_ELEMENT;
	public static final VertexFormatElement ENTITY_ID_ELEMENT;
	public static final VertexFormatElement MID_TEXTURE_ELEMENT;
	public static final VertexFormatElement TANGENT_ELEMENT;
	public static final VertexFormatElement MID_BLOCK_ELEMENT;

	public static final VertexFormat TERRAIN;
	public static final VertexFormat ENTITY;
	public static final VertexFormat GLYPH;
	public static final VertexFormat CLOUDS;

	static {
		ENTITY_ELEMENT = new VertexFormatElement(10, VertexFormatElement.Format.SHORT, VertexFormatElement.Type.POSITION, 2);
		ENTITY_ID_ELEMENT = new VertexFormatElement(11, VertexFormatElement.Format.UNSIGNED_SHORT, VertexFormatElement.Type.UV, 3);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(12, VertexFormatElement.Format.FLOAT, VertexFormatElement.Type.UV, 2);
		TANGENT_ELEMENT = new VertexFormatElement(13, VertexFormatElement.Format.BYTE, VertexFormatElement.Type.POSITION, 4);
		MID_BLOCK_ELEMENT = new VertexFormatElement(14, VertexFormatElement.Format.BYTE, VertexFormatElement.Type.POSITION, 3);

		TERRAIN = new VertexFormat();

		TERRAIN.addElement(VertexFormats.POSITION_ELEMENT);
		TERRAIN.addElement(VertexFormats.COLOR_ELEMENT);
		TERRAIN.addElement(VertexFormats.TEXTURE_FLOAT_ELEMENT);
		TERRAIN.addElement(VertexFormats.TEXTURE_SHORT_ELEMENT);
		TERRAIN.addElement(VertexFormats.NORMAL_ELEMENT);
        TERRAIN.addElement(VertexFormats.PADDING_ELEMENT);
		TERRAIN.addElement(ENTITY_ELEMENT);
		TERRAIN.addElement(MID_TEXTURE_ELEMENT);
		TERRAIN.addElement(TANGENT_ELEMENT);
		TERRAIN.addElement(MID_BLOCK_ELEMENT);
        TERRAIN.addElement(VertexFormats.PADDING_ELEMENT);

		ENTITY = new VertexFormat();
		ENTITY.addElement(VertexFormats.POSITION_ELEMENT);
		ENTITY.addElement(VertexFormats.COLOR_ELEMENT);
		ENTITY.addElement(VertexFormats.TEXTURE_FLOAT_ELEMENT);
		ENTITY.addElement(VertexFormats.TEXTURE_SHORT_ELEMENT);
		ENTITY.addElement(VertexFormats.NORMAL_ELEMENT);
        ENTITY.addElement(VertexFormats.PADDING_ELEMENT);
		ENTITY.addElement(ENTITY_ID_ELEMENT);
		ENTITY.addElement(MID_TEXTURE_ELEMENT);
		ENTITY.addElement(TANGENT_ELEMENT);
        
		GLYPH = new VertexFormat();
		GLYPH.addElement(VertexFormats.POSITION_ELEMENT);
		GLYPH.addElement(VertexFormats.COLOR_ELEMENT);
		GLYPH.addElement(VertexFormats.TEXTURE_FLOAT_ELEMENT);
		GLYPH.addElement(VertexFormats.TEXTURE_SHORT_ELEMENT);
		GLYPH.addElement(VertexFormats.NORMAL_ELEMENT);
        GLYPH.addElement(VertexFormats.PADDING_ELEMENT);
		GLYPH.addElement(ENTITY_ID_ELEMENT);
		GLYPH.addElement(MID_TEXTURE_ELEMENT);
		GLYPH.addElement(TANGENT_ELEMENT);
        GLYPH.addElement(VertexFormats.PADDING_ELEMENT);

		CLOUDS = new VertexFormat();
		CLOUDS.addElement(VertexFormats.POSITION_ELEMENT);
		CLOUDS.addElement(VertexFormats.COLOR_ELEMENT);
		CLOUDS.addElement(VertexFormats.NORMAL_ELEMENT);
        CLOUDS.addElement(VertexFormats.PADDING_ELEMENT);
	}

	private static void debug(VertexFormat format) {
		Iris.logger.info("Vertex format: " + format + " with byte size " + format.getVertexSize());
		int byteIndex = 0;
		for (VertexFormatElement element : format.getElements()) {
			Iris.logger.info(element + " @ " + byteIndex + " is " + element.getFormat() + " " + element.getType());
			byteIndex += element.getSize();
		}
	}
}
