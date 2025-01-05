package net.coderbot.iris.texture.pbr.loader;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PBRTextureLoaderRegistry {
	public static final PBRTextureLoaderRegistry INSTANCE = new PBRTextureLoaderRegistry();

	static {
		INSTANCE.register(ResourceTexture.class, new SimplePBRLoader());
		INSTANCE.register(SpriteAtlasTexture.class, new AtlasPBRLoader());
	}

	private final Map<Class<?>, PBRTextureLoader<?>> loaderMap = new HashMap<>();

	public <T extends AbstractTexture> void register(Class<? extends T> clazz, PBRTextureLoader<T> loader) {
		loaderMap.put(clazz, loader);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends AbstractTexture> PBRTextureLoader<T> getLoader(Class<? extends T> clazz) {
		return (PBRTextureLoader<T>) loaderMap.get(clazz);
	}
}
