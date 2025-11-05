package dev.vexor.radium.compat.mojang.minecraft.gui.draw;

public class Rect2i {
    private int xPos;
    private int yPos;
    private int width;
    private int height;

    public Rect2i(int n, int n2, int n3, int n4) {
        this.xPos = n;
        this.yPos = n2;
        this.width = n3;
        this.height = n4;
    }

    public int getX() {
        return this.xPos;
    }

    public int getY() {
        return this.yPos;
    }

    public void setX(int n) {
        this.xPos = n;
    }

    public void setY(int n) {
        this.yPos = n;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setWidth(int n) {
        this.width = n;
    }

    public void setHeight(int n) {
        this.height = n;
    }

    public boolean contains(int n, int n2) {
        return n >= this.xPos && n <= this.xPos + this.width && n2 >= this.yPos && n2 <= this.yPos + this.height;
    }
}
