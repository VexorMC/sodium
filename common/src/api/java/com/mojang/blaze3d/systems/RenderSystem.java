package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.BlendFactor;

public class RenderSystem {
    public static void disableAlphaTest() {
        GlStateManager.disableAlphaTest();
    }

    public static void enableAlphaTest() {
        GlStateManager.enableAlphaTest();
    }

    public static void disableDepthTest() {
        GlStateManager.disableDepthTest();
    }

    public static void enableDepthTest() {
        GlStateManager.enableDepthTest();
    }

    public static void depthMask(boolean bl) {
        GlStateManager.depthMask(bl);
    }

    public static void enableBlend() {
        GlStateManager.enableBlend();
    }

    public static void disableBlend() {
        GlStateManager.disableBlend();
    }

    public static void blendFuncSeparate(int n, int n2, int n3, int n4) {
        GlStateManager.blendFuncSeparate(n, n2, n3, n4);
    }

    public static void blendColor(float f, float f2, float f3, float f4) {
        GlStateManager.color(f, f2, f3, f4);
    }

    public static void enableCull() {
        GlStateManager.enableCull();
    }

    public static void disableCull() {
        GlStateManager.disableCull();
    }

    public static void activeTexture(int n) {
        GlStateManager.activeTexture(n);
    }

    public static void enableTexture() {
        GlStateManager.enableTexture();
    }

    public static void disableTexture() {
        GlStateManager.disableTexture();
    }

    public static void bindTexture(int n) {
        GlStateManager.bindTexture(n);
    }

    public static void viewport(int n, int n2, int n3, int n4) {
        GlStateManager.viewport(n, n2, n3, n4);
    }

    public static void clearColor(float f, float f2, float f3, float f4) {
        GlStateManager.clearColor(f, f2, f3, f4);
    }

    public static void clear(int n) {
        GlStateManager.clear(n);
    }

    public static void matrixMode(int n) {
        GlStateManager.matrixMode(n);
    }

    public static void loadIdentity() {
        GlStateManager.loadIdentity();
    }

    public static void pushMatrix() {
        GlStateManager.pushMatrix();
    }

    public static void popMatrix() {
        GlStateManager.popMatrix();
    }

    public static void ortho(double d, double d2, double d3, double d4, double d5, double d6) {
        GlStateManager.ortho(d, d2, d3, d4, d5, d6);
    }

    public static void scalef(float f, float f2, float f3) {
        GlStateManager.scale(f, f2, f3);
    }

    public static void translatef(float f, float f2, float f3) {
        GlStateManager.translate(f, f2, f3);
    }

    public static void color4f(float f, float f2, float f3, float f4) {
        GlStateManager.color(f, f2, f3, f4);
    }

    public static void color3f(float f, float f2, float f3) {
        GlStateManager.color(f, f2, f3, 1.0f);
    }

    public static void defaultBlendFunc() {
        RenderSystem.blendFuncSeparate(BlendFactor.SourceFactor.SRC_ALPHA, BlendFactor.DestFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.SourceFactor.ONE, BlendFactor.DestFactor.ZERO);
    }
}

