package net.coderbot.iris.rendertarget;

public interface Blaze3dRenderTargetExt {
	int iris$getDepthBufferVersion();

	int iris$getColorBufferVersion();

    boolean getIris$useDepth();
    int getIris$depthTextureId();
}
