package net.coderbot.iris.texture.pbr.loader;

import net.coderbot.iris.mixin.texture.SimpleTextureAccessor;
import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SimplePBRLoader implements PBRTextureLoader<ResourceTexture> {
	@Override
	public void load(ResourceTexture texture, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer) {
		Identifier location = ((SimpleTextureAccessor) texture).getLocation();

		AbstractTexture normalTexture = createPBRTexture(location, resourceManager, PBRType.NORMAL);
		AbstractTexture specularTexture = createPBRTexture(location, resourceManager, PBRType.SPECULAR);

		if (normalTexture != null) {
			pbrTextureConsumer.acceptNormalTexture(normalTexture);
		}
		if (specularTexture != null) {
			pbrTextureConsumer.acceptSpecularTexture(specularTexture);
		}
	}

	@Nullable
	protected AbstractTexture createPBRTexture(Identifier imageLocation, ResourceManager resourceManager, PBRType pbrType) {
		Identifier pbrImageLocation = pbrType.appendToFileLocation(imageLocation);

		ResourceTexture pbrTexture = new ResourceTexture(pbrImageLocation);
		try {
			pbrTexture.load(resourceManager);
		} catch (IOException e) {
			return null;
		}

		return pbrTexture;
	}
}
