package net.caffeinemc.mods.sodium.client.render.vertex;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VertexConsumerTracker {
    private static final Logger LOGGER = LogManager.getLogger("Sodium-VertexConsumerTracker");
    private static final ReferenceSet<Class<? extends VertexConsumer>> BAD_CONSUMERS = ReferenceSets.synchronize(new ReferenceOpenHashSet<>());

    public static void logBadConsumer(VertexConsumer consumer) {
        if (BAD_CONSUMERS.add(consumer.getClass())) {
            LOGGER.warn("Class {} does not support optimized vertex writing code paths, which may cause reduced rendering performance",
                    consumer.getClass().getName());
        }
    }
}
