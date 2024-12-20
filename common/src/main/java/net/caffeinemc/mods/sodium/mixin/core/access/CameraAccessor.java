package net.caffeinemc.mods.sodium.mixin.core.access;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.FloatBuffer;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("PROJECTION_MATRIX")
    public static FloatBuffer getProjectionMatrix() { throw new AssertionError(); }

    @Accessor("MODEL_MATRIX")
    public static FloatBuffer getModelMatrix() { throw new AssertionError(); }
}
