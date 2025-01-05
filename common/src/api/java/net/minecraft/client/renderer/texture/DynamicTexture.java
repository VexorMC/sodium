package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.resource.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DynamicTexture
        extends AbstractTexture  {
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private NativeImage pixels;

    public DynamicTexture(NativeImage nativeImage) {
        this.pixels = nativeImage;
        TextureUtil.prepareImage(this.getGlId(), this.pixels.getWidth(), this.pixels.getHeight());
        this.upload();
    }

    public DynamicTexture(int n, int n2, boolean bl) {
        this.pixels = new NativeImage(n, n2, bl);
        TextureUtil.prepareImage(this.getGlId(), this.pixels.getWidth(), this.pixels.getHeight());
    }

    @Override
    public void load(ResourceManager resourceManager) {
    }

    public void upload() {
        if (this.pixels != null) {
            this.bind();
            this.pixels.upload(0, 0, 0, false);
        } else {
            LOGGER.warn("Trying to upload disposed texture {}", this.getGlId());
        }
    }

    @Nullable
    public NativeImage getPixels() {
        return this.pixels;
    }

    public void setPixels(NativeImage nativeImage) {
        if (this.pixels != null) {
            this.pixels.close();
        }
        this.pixels = nativeImage;
    }

    public void bind() {
        GlStateManager.bindTexture(getGlId());
    }

    public void clearGlId() {
        if (this.pixels != null) {
            this.pixels.close();
            super.clearGlId();
            this.pixels = null;
        }
    }
}

