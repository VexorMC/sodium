package net.coderbot.iris.gl.framebuffer;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.nio.IntBuffer;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.texture.TextureInfoCache;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public class GlFramebuffer extends GlResource {
    private final Int2IntMap attachments;
    private final int maxDrawBuffers;
    private final int maxColorAttachments;
    private boolean hasDepthAttachment;

    public GlFramebuffer() {
        super(IrisRenderSystem.createFramebuffer());

        this.attachments = new Int2IntArrayMap();
        this.maxDrawBuffers = GL11.glGetInteger(GL20.GL_MAX_DRAW_BUFFERS);
        this.maxColorAttachments = GL11.glGetInteger(GL30.GL_MAX_COLOR_ATTACHMENTS);
        this.hasDepthAttachment = false;
    }

    public void addDepthAttachment(int texture) {
        final int internalFormat = TextureInfoCache.INSTANCE.getInfo(texture).getInternalFormat();
        final DepthBufferFormat depthBufferFormat = DepthBufferFormat.fromGlEnumOrDefault(internalFormat);

        final int fb = getGlId();

        if (depthBufferFormat.isCombinedStencil()) {
            IrisRenderSystem.framebufferTexture2D(fb, GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, texture, 0);
        } else {
            IrisRenderSystem.framebufferTexture2D(fb, GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, texture, 0);
        }

        this.hasDepthAttachment = true;
    }

    public void addColorAttachment(int index, int texture) {
        final int fb = getGlId();

        IrisRenderSystem.framebufferTexture2D(fb, GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + index, GL11.GL_TEXTURE_2D, texture, 0);
        attachments.put(index, texture);
    }

    public void noDrawBuffers() {
        IrisRenderSystem.drawBuffers(getGlId(), new int[] {0});
    }

    public void drawBuffers(int[] buffers) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer glBuffers = stack.mallocInt(buffers.length);
            int index = 0;

            if (buffers.length > maxDrawBuffers) {
                throw new IllegalArgumentException("Cannot write to more than " + maxDrawBuffers + " draw buffers on this GPU");
            }
            for (int buffer : buffers) {
                if (buffer >= maxColorAttachments) {
                    throw new IllegalArgumentException("Only " + maxColorAttachments + " color attachments are supported on this GPU, but an attempt was made to write to a color attachment with index " + buffer);
                }
                glBuffers.put(index++, GL30.GL_COLOR_ATTACHMENT0 + buffer);
            }
            IrisRenderSystem.drawBuffers(getGlId(), glBuffers);
        }
    }

    public void readBuffer(int buffer) {
        IrisRenderSystem.readBuffer(getGlId(), GL30.GL_COLOR_ATTACHMENT0 + buffer);
    }

    public int getColorAttachment(int index) {
        return attachments.get(index);
    }

    public boolean hasDepthAttachment() {
        return hasDepthAttachment;
    }

    public void bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, getGlId());
    }

    public void bindAsReadBuffer() {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, getGlId());
    }

    public void bindAsDrawBuffer() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, getGlId());
    }

    protected void destroyInternal() {
        GL30.glDeleteFramebuffers(getGlId());
    }

    public boolean isComplete() {
        bind();

        return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
    }

    public int getStatus() {
        bind();

        return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
    }

    public int getId() {
        return getGlId();
    }
}