package net.caffeinemc.mods.sodium.client.render.chunk.async;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.DeferredTaskList;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.TaskCollectingTree;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.CullType;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.RayOcclusionSectionTree;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.SectionTree;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.world.World;

import java.util.Collection;

public class CullTask extends AsyncRenderTask<CullResult> {
    protected final OcclusionCuller occlusionCuller;
    protected final boolean useOcclusionCulling;
    private final float searchDistanceRegular;
    private final float searchDistanceLocal;
    private ReferenceOpenHashSet<RenderSection> presentPatches;
    private final World level;

    public CullTask(Viewport viewport, float searchDistanceRegular, float searchDistanceLocal, int frame, OcclusionCuller occlusionCuller, boolean useOcclusionCulling, World level) {
        super(viewport, frame);
        this.searchDistanceRegular = searchDistanceRegular;
        this.searchDistanceLocal = searchDistanceLocal;
        this.occlusionCuller = occlusionCuller;
        this.useOcclusionCulling = useOcclusionCulling;
        this.level = level;
    }

    @Override
    protected CullResult runTask() {
        var wideTree = new TaskCollectingTree(this.viewport, this.searchDistanceRegular, this.frame, CullType.WIDE, this.level);
        var regularTree = new SectionTree(this.viewport, this.searchDistanceRegular, this.frame, CullType.REGULAR, this.level);
        var localTree = new RayOcclusionSectionTree(this.viewport, this.searchDistanceLocal, this.frame, CullType.LOCAL, this.level);

        this.occlusionCuller.findVisible(wideTree, regularTree, localTree, this.viewport, this.searchDistanceRegular, this.searchDistanceLocal, this.useOcclusionCulling, this);

        wideTree.prepareForTraversal();
        regularTree.prepareForTraversal();
        localTree.prepareForTraversal();

        var taskLists = wideTree.getPendingTaskLists();

        return new CullResult() {
            @Override
            public SectionTree getCullTreeWide() {
                CullTask.this.applyPresentPatches(wideTree);
                return wideTree;
            }

            @Override
            public SectionTree getCullTreeRegular() {
                CullTask.this.applyPresentPatches(regularTree);
                return regularTree;
            }

            @Override
            public SectionTree getCullTreeLocal() {
                CullTask.this.applyPresentPatches(localTree);
                return localTree;
            }

            @Override
            public DeferredTaskList getPendingTaskLists() {
                return taskLists;
            }
        };
    }

    @Override
    public void registerPresentPatches(Collection<RenderSection> presentPatches) {
        if (this.presentPatches == null) {
            this.presentPatches = new ReferenceOpenHashSet<>();
        }
        this.presentPatches.addAll(presentPatches);
    }

    protected void applyPresentPatches(SectionTree result) {
        if (this.presentPatches == null) {
            return;
        }

        for (var section : this.presentPatches) {
            var x = section.getChunkX();
            var y = section.getChunkY();
            var z = section.getChunkZ();

            result.patchMarkPresent(x, y, z);
        }
    }
}
