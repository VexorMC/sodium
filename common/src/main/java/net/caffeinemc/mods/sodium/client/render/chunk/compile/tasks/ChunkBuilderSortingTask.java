package net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks;

import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.Sorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.Profiler;
import org.joml.Vector3dc;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkSortOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.DynamicData;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.estimation.MeshTaskSizeEstimator;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.DynamicSorter;

public class ChunkBuilderSortingTask extends ChunkBuilderTask<ChunkSortOutput> {
    private final DynamicSorter sorter;

    public ChunkBuilderSortingTask(RenderSection render, int frame, Vector3dc absoluteCameraPos, DynamicSorter sorter) {
        super(render, frame, absoluteCameraPos);
        this.sorter = sorter;
    }

    @Override
    public ChunkSortOutput execute(ChunkBuildContext context, CancellationToken cancellationToken) {
        if (cancellationToken.isCancelled()) {
            return null;
        }

        Profiler profiler = MinecraftClient.getInstance().profiler;
        profiler.push("translucency sorting");

        this.sorter.writeIndexBuffer(this, false);

        profiler.pop();
        return new ChunkSortOutput(this.render, this.submitTime, this.sorter);
    }

    public static ChunkBuilderSortingTask createTask(RenderSection render, int frame, Vector3dc absoluteCameraPos) {
        if (render.getTranslucentData() instanceof DynamicData dynamicData) {
            return new ChunkBuilderSortingTask(render, frame, absoluteCameraPos, dynamicData.getSorter());
        }
        return null;
    }

    @Override
    public long estimateTaskSizeWith(MeshTaskSizeEstimator estimator) {
        return this.sorter.getQuadCount();
    }
}
