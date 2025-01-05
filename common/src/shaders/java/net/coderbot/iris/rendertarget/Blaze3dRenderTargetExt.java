package net.coderbot.iris.rendertarget;

public interface Blaze3dRenderTargetExt {
	int iris$getDepthBufferVersion();

	int iris$getColorBufferVersion();

    public boolean getIris$useDepth();
    public int getIris$depthTextureId();
}
