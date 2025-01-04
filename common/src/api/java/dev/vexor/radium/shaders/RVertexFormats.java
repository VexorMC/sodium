package dev.vexor.radium.shaders;

import net.minecraft.client.render.VertexFormat;

import static net.minecraft.client.render.VertexFormats.POSITION_ELEMENT;

public class RVertexFormats {
    public static final VertexFormat BLIT_SCREEN = new VertexFormat();

    static {
        BLIT_SCREEN.addElement(POSITION_ELEMENT);
    }
}
