package net.minecraft.client.renderer;

public enum VertexFormatMode {
    LINES(4, 2, 2, false),
    LINE_STRIP(5, 2, 1, true),
    DEBUG_LINES(1, 2, 2, false),
    DEBUG_LINE_STRIP(3, 2, 1, true),
    TRIANGLES(4, 3, 3, false),
    TRIANGLE_STRIP(5, 3, 1, true),
    TRIANGLE_FAN(6, 3, 1, true),
    QUADS(4, 4, 4, false);

    public final int asGLMode;
    public final int primitiveLength;
    public final int primitiveStride;
    public final boolean connectedPrimitives;

    VertexFormatMode(int n2, int n3, int n4, boolean bl) {
        this.asGLMode = n2;
        this.primitiveLength = n3;
        this.primitiveStride = n4;
        this.connectedPrimitives = bl;
    }

    public int indexCount(int n) {
        return switch (this.ordinal()) {
            case 1, 2, 3, 4, 5, 6 -> n;
            case 0, 7 -> n / 4 * 6;
            default -> 0;
        };
    }
}
