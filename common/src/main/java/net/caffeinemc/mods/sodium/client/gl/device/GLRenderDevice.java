package net.caffeinemc.mods.sodium.client.gl.device;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.system.MemoryUtil;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.array.GlVertexArray;
import net.caffeinemc.mods.sodium.client.gl.buffer.*;
import net.caffeinemc.mods.sodium.client.gl.functions.DeviceFunctions;
import net.caffeinemc.mods.sodium.client.gl.state.GlStateTracker;
import net.caffeinemc.mods.sodium.client.gl.sync.GlFence;
import net.caffeinemc.mods.sodium.client.gl.tessellation.*;
import net.caffeinemc.mods.sodium.client.gl.util.EnumBitField;
import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GLRenderDevice implements RenderDevice {
    private final GlStateTracker stateTracker = new GlStateTracker();
    private final CommandList commandList = new ImmediateCommandList(this.stateTracker);
    private final DrawCommandList drawCommandList = new ImmediateDrawCommandList();

    private final DeviceFunctions functions = new DeviceFunctions(this);

    private boolean isActive;
    private GlTessellation activeTessellation;

    @Override
    public CommandList createCommandList() {
        GLRenderDevice.this.checkDeviceActive();

        return this.commandList;
    }

    @Override
    public void makeActive() {
        if (this.isActive) {
            return;
        }

        this.stateTracker.clear();
        this.isActive = true;
    }

    @Override
    public void makeInactive() {
        if (!this.isActive) {
            return;
        }

        this.stateTracker.clear();
        this.isActive = false;

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public ContextCapabilities getCapabilities() {
        return GLContext.getCapabilities();
    }

    @Override
    public DeviceFunctions getDeviceFunctions() {
        return this.functions;
    }

    private void checkDeviceActive() {
        if (!this.isActive) {
            throw new IllegalStateException("Tried to access device from unmanaged context");
        }
    }

    private class ImmediateCommandList implements CommandList {
        private final GlStateTracker stateTracker;

        private ImmediateCommandList(GlStateTracker stateTracker) {
            this.stateTracker = stateTracker;
        }

        @Override
        public void bindVertexArray(GlVertexArray array) {
            if (this.stateTracker.makeVertexArrayActive(array)) {
                GL30.glBindVertexArray(array.handle());
            }
        }

        @Override
        public void uploadData(GlMutableBuffer glBuffer, ByteBuffer byteBuffer, GlBufferUsage usage) {
            this.bindBuffer(GlBufferTarget.ARRAY_BUFFER, glBuffer);

            GL15.glBufferData(GlBufferTarget.ARRAY_BUFFER.getTargetParameter(), byteBuffer, usage.getId());
            glBuffer.setSize(byteBuffer.remaining());
        }

        @Override
        public void copyBufferSubData(GlBuffer src, GlBuffer dst, long readOffset, long writeOffset, long bytes) {
            this.bindBuffer(GlBufferTarget.COPY_READ_BUFFER, src);
            this.bindBuffer(GlBufferTarget.COPY_WRITE_BUFFER, dst);

            GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, readOffset, writeOffset, bytes);
        }

        @Override
        public void bindBuffer(GlBufferTarget target, GlBuffer buffer) {
            if (this.stateTracker.makeBufferActive(target, buffer)) {
                GL15.glBindBuffer(target.getTargetParameter(), buffer.handle());
            }
        }

        @Override
        public void unbindVertexArray() {
            if (this.stateTracker.makeVertexArrayActive(null)) {
                GL30.glBindVertexArray(GlVertexArray.NULL_ARRAY_ID);
            }
        }

        @Override
        public void allocateStorage(GlMutableBuffer buffer, long bufferSize, GlBufferUsage usage) {
            this.bindBuffer(GlBufferTarget.ARRAY_BUFFER, buffer);

            GL15.glBufferData(GlBufferTarget.ARRAY_BUFFER.getTargetParameter(), bufferSize, usage.getId());
            buffer.setSize(bufferSize);
        }

        @Override
        public void deleteBuffer(GlBuffer buffer) {
            if (buffer.getActiveMapping() != null) {
                this.unmap(buffer.getActiveMapping());
            }

            this.stateTracker.notifyBufferDeleted(buffer);

            int handle = buffer.handle();
            buffer.invalidateHandle();

            GL15.glDeleteBuffers(handle);
        }

        @Override
        public void deleteVertexArray(GlVertexArray vertexArray) {
            this.stateTracker.notifyVertexArrayDeleted(vertexArray);

            int handle = vertexArray.handle();
            vertexArray.invalidateHandle();

            GL30.glDeleteVertexArrays(handle);
        }

        @Override
        public void flush() {
            // NO-OP
        }

        @Override
        public DrawCommandList beginTessellating(GlTessellation tessellation) {
            GLRenderDevice.this.activeTessellation = tessellation;
            GLRenderDevice.this.activeTessellation.bind(GLRenderDevice.this.commandList);

            return GLRenderDevice.this.drawCommandList;
        }

        @Override
        public void deleteTessellation(GlTessellation tessellation) {
            tessellation.delete(this);
        }


        /**
         * CHANGED SOME STUFF!! POSSIBLE BEHAVIOUR CHANGE!!!
         * */

        @Override
        public GlBufferMapping mapBuffer(GlBuffer buffer, long offset, long length, EnumBitField<GlBufferMapFlags> flags) {
            if (buffer.getActiveMapping() != null) {
                throw new IllegalStateException("Buffer is already mapped");
            }

            // Cache the flags bitfield value to avoid repeated bit operations
            final int flagBits = flags.getBitField();

            // Check GL44 support if persistent mapping is requested
            if ((flagBits & GlBufferMapFlags.PERSISTENT.getBits()) != 0 &&
                    !GLContext.getCapabilities().OpenGL44) {
                throw new IllegalStateException("Persistent buffer mapping requires OpenGL 4.4");
            }

            // Fast path for mutable buffers (most common case)
            if (!(buffer instanceof GlImmutableBuffer)) {
                // Only need to check persistent flag for mutable buffers
                if ((flagBits & GlBufferMapFlags.PERSISTENT.getBits()) != 0) {
                    throw new IllegalStateException("Tried to map mutable buffer as persistent");
                }
            } else {
                // Immutable buffer validation using raw bit operations
                int bufferFlags = ((GlImmutableBuffer) buffer).getFlags().getBitField();

                // Combine all checks into a single bit operation
                int requiredFlags = 0;
                if ((flagBits & GlBufferMapFlags.PERSISTENT.getBits()) != 0)
                    requiredFlags |= GlBufferStorageFlags.PERSISTENT.getBits();
                if ((flagBits & GlBufferMapFlags.WRITE.getBits()) != 0)
                    requiredFlags |= GlBufferStorageFlags.MAP_WRITE.getBits();
                if ((flagBits & GlBufferMapFlags.READ.getBits()) != 0)
                    requiredFlags |= GlBufferStorageFlags.MAP_READ.getBits();

                if ((bufferFlags & requiredFlags) != requiredFlags) {
                    throw new IllegalStateException("Buffer mapping flags incompatible with storage flags");
                }
            }

            // Bind and map in one go
            GL15.glBindBuffer(GlBufferTarget.ARRAY_BUFFER.getTargetParameter(), buffer.handle());
            ByteBuffer buf = GL30.glMapBufferRange(GlBufferTarget.ARRAY_BUFFER.getTargetParameter(),
                    offset, length, flagBits, null);

            if (buf == null) {
                throw new RuntimeException("Failed to map buffer");
            }

            GlBufferMapping mapping = new GlBufferMapping(buffer, buf);
            buffer.setActiveMapping(mapping);
            return mapping;
        }

        @Override
        public void unmap(GlBufferMapping map) {
            checkMapDisposed(map);

            GlBuffer buffer = map.getBufferObject();

            this.bindBuffer(GlBufferTarget.ARRAY_BUFFER, buffer);
            GL15.glUnmapBuffer(GlBufferTarget.ARRAY_BUFFER.getTargetParameter());

            buffer.setActiveMapping(null);
            map.dispose();
        }

        @Override
        public void flushMappedRange(GlBufferMapping map, int offset, int length) {
            checkMapDisposed(map);

            GlBuffer buffer = map.getBufferObject();

            this.bindBuffer(GlBufferTarget.COPY_READ_BUFFER, buffer);
            GL30.glFlushMappedBufferRange(GlBufferTarget.COPY_READ_BUFFER.getTargetParameter(), offset, length);
        }

        @Override
        public GlFence createFence() {
            return new GlFence(GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0));
        }

        private void checkMapDisposed(GlBufferMapping map) {
            if (map.isDisposed()) {
                throw new IllegalStateException("Buffer mapping is already disposed");
            }
        }

        @Override
        public GlMutableBuffer createMutableBuffer() {
            return new GlMutableBuffer();
        }

        @Override
        public GlImmutableBuffer createImmutableBuffer(long bufferSize, EnumBitField<GlBufferStorageFlags> flags) {
            GlImmutableBuffer buffer = new GlImmutableBuffer(flags);

            this.bindBuffer(GlBufferTarget.ARRAY_BUFFER, buffer);
            GLRenderDevice.this.functions.getBufferStorageFunctions()
                    .createBufferStorage(GlBufferTarget.ARRAY_BUFFER, bufferSize, flags);

            return buffer;
        }

        @Override
        public GlTessellation createTessellation(GlPrimitiveType primitiveType, TessellationBinding[] bindings) {
            GlVertexArrayTessellation tessellation = new GlVertexArrayTessellation(new GlVertexArray(), primitiveType, bindings);
            tessellation.init(this);

            return tessellation;
        }
    }

    private class ImmediateDrawCommandList implements DrawCommandList {
        public ImmediateDrawCommandList() {

        }

        @Override
        public void multiDrawElementsBaseVertex(MultiDrawBatch batch, GlIndexType indexType) {
            GlPrimitiveType primitiveType = GLRenderDevice.this.activeTessellation.getPrimitiveType();

            for (int i = 0; i < batch.size(); i++) {
                int elementCount = MemoryUtil.memGetInt(batch.pElementCount + i * Integer.BYTES);
                long elementPointer = MemoryUtil.memGetAddress(batch.pElementPointer + i * Long.BYTES);
                int baseVertex = MemoryUtil.memGetInt(batch.pBaseVertex + i * Integer.BYTES);

                GL32.glDrawElementsBaseVertex(
                        primitiveType.getId(),
                        elementCount,
                        indexType.getFormatId(),
                        elementPointer,
                        baseVertex
                );
            }
        }

        @Override
        public void endTessellating() {
            GLRenderDevice.this.activeTessellation.unbind(GLRenderDevice.this.commandList);
            GLRenderDevice.this.activeTessellation = null;
        }

        @Override
        public void flush() {
            if (GLRenderDevice.this.activeTessellation != null) {
                this.endTessellating();
            }
        }
    }
}
