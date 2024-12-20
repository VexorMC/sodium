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

    public Rect2i intersect(Rect2i rect2i) {
        int n = this.xPos;
        int n2 = this.yPos;
        int n3 = this.xPos + this.width;
        int n4 = this.yPos + this.height;
        int n5 = rect2i.getX();
        int n6 = rect2i.getY();
        int n7 = n5 + rect2i.getWidth();
        int n8 = n6 + rect2i.getHeight();
        this.xPos = Math.max(n, n5);
        this.yPos = Math.max(n2, n6);
        this.width = Math.max(0, Math.min(n3, n7) - this.xPos);
        this.height = Math.max(0, Math.min(n4, n8) - this.yPos);
        return this;
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

    public void setPosition(int n, int n2) {
        this.xPos = n;
        this.yPos = n2;
    }

    public boolean contains(int n, int n2) {
        return n >= this.xPos && n <= this.xPos + this.width && n2 >= this.yPos && n2 <= this.yPos + this.height;
    }
}
