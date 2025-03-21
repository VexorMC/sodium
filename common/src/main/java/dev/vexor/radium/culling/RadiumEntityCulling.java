package dev.vexor.radium.culling;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import dev.vexor.radium.Hooks;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;

public class RadiumEntityCulling {

    public static RadiumEntityCulling INSTANCE = new RadiumEntityCulling();
    public OcclusionCullingInstance culling;
    public static boolean enabled = true; // public static to make it faster for the jvm
    public CullTask cullTask;
    private Thread cullThread;

	public int renderedBlockEntities = 0;
	public int skippedBlockEntities = 0;
	public int renderedEntities = 0;
	public int skippedEntities = 0;

	public void onInitialize() {
        culling = new OcclusionCullingInstance(SodiumClientMod.options().culling.tracingDistance, new RadiumCullingDataProvider());
        cullTask = new CullTask(culling, SodiumClientMod.options().culling.blockEntityWhitelist);

		cullThread = new Thread(cullTask, "CullThread");

		cullThread.setUncaughtExceptionHandler((thread, ex) -> {
			System.out.println("The CullingThread has crashed! Please report the following stacktrace!");
			ex.printStackTrace();
		});

		cullThread.start();

        Hooks.CLIENT_TICK.add(this::clientTick);
        Hooks.WORLD_TICK.add(this::worldTick);
	}
    
    public void worldTick() {
        cullTask.requestCull = true;
    }
    
    public void clientTick() {
        cullTask.requestCull = true;
    }
}
