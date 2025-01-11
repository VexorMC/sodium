package net.coderbot.iris.texture.format;

import net.coderbot.iris.Iris;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class TextureFormatLoader {
	public static final Identifier LOCATION = new Identifier("optifine/texture.properties");

	private static TextureFormat format;

	@Nullable
	public static TextureFormat getFormat() {
		return format;
	}

	public static void reload(ResourceManager resourceManager) {
		TextureFormat newFormat = loadFormat(resourceManager);
		boolean didFormatChange = !Objects.equals(format, newFormat);
		format = newFormat;
		if (didFormatChange) {
			onFormatChange();
		}
	}

	@Nullable
	private static TextureFormat loadFormat(ResourceManager resourceManager) {
		try  {
            Resource resource = resourceManager.getResource(LOCATION);
			Properties properties = new Properties();
			properties.load(resource.getInputStream());
			String format = properties.getProperty("format");
			if (format != null && !format.isEmpty()) {
				String[] splitFormat = format.split("/");
				if (splitFormat.length > 0) {
					String name = splitFormat[0];
					TextureFormat.Factory factory = TextureFormatRegistry.INSTANCE.getFactory(name);
					if (factory != null) {
						String version;
						if (splitFormat.length > 1) {
							version = splitFormat[1];
						} else {
							version = null;
						}
						return factory.createFormat(name, version);
					} else {
						Iris.logger.warn("Invalid texture format '" + name + "' in file '" + LOCATION + "'");
					}
				}
			}
		}  catch (FileNotFoundException e) {
			//
		} catch (Exception e) {
			Iris.logger.error("Failed to load texture format from file '" + LOCATION + "'", e);
		}
		return null;
	}

	private static void onFormatChange() {
		try {
			Iris.reload();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
