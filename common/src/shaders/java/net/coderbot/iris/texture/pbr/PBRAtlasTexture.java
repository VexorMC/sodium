package net.coderbot.iris.texture.pbr;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.mixin.texture.TextureAtlasSpriteAccessor;
import net.coderbot.iris.texture.util.TextureExporter;
import net.coderbot.iris.texture.util.TextureManipulationUtil;
import net.minecraft.client.resource.AnimationMetadata;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PBRAtlasTexture extends AbstractTexture {
	protected final SpriteAtlasTexture atlasTexture;
	protected final PBRType type;
	protected final Identifier id;
	protected final Map<Identifier, Sprite> sprites = new HashMap<>();
	protected final Set<Sprite> animatedSprites = new HashSet<>();

	public PBRAtlasTexture(SpriteAtlasTexture atlasTexture, PBRType type) {
		this.atlasTexture = atlasTexture;
		this.type = type;
		id = type.appendToFileLocation(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
	}

	public PBRType getType() {
		return type;
	}

	public Identifier getAtlasId() {
		return id;
	}

	public void addSprite(Sprite sprite) {
		sprites.put(new Identifier(sprite.getName()), sprite);
		if (sprite.hasMeta()) {
			animatedSprites.add(sprite);
		}
	}

	@Nullable
	public Sprite getSprite(Identifier id) {
		return sprites.get(id);
	}

	public void clear() {
		sprites.clear();
		animatedSprites.clear();
	}

	public void upload(int atlasWidth, int atlasHeight, int mipLevel) {
		int glId = getGlId();
		net.minecraft.client.texture.TextureUtil.prepareImage(glId, mipLevel, atlasWidth, atlasHeight);
		TextureManipulationUtil.fillWithColor(glId, mipLevel, type.getDefaultValue());

		for (Sprite sprite : sprites.values()) {
			try {
				uploadSprite(sprite);
			} catch (Throwable throwable) {
				CrashReport crashReport = CrashReport.create(throwable, "Stitching texture atlas");
				CrashReportSection crashReportCategory = crashReport.addElement("Texture being stitched together");
				crashReportCategory.add("Atlas path", id);
				crashReportCategory.add("Sprite", sprite);
				throw new CrashException(crashReport);
			}
		}

		if (!animatedSprites.isEmpty()) {
			PBRAtlasHolder pbrHolder = ((TextureAtlasExtension) atlasTexture).getOrCreatePBRHolder();
			switch (type) {
			case NORMAL:
				pbrHolder.setNormalAtlas(this);
				break;
			case SPECULAR:
				pbrHolder.setSpecularAtlas(this);
				break;
			}
		}

		if (PBRTextureManager.DEBUG) {
			TextureExporter.exportTextures("pbr_debug/atlas", id.getNamespace() + "_" + id.getPath().replaceAll("/", "_"), glId, mipLevel, atlasWidth, atlasHeight);
		}
	}

	public boolean tryUpload(int atlasWidth, int atlasHeight, int mipLevel) {
		try {
			upload(atlasWidth, atlasHeight, mipLevel);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	protected void uploadSprite(Sprite sprite) {
		if (sprite.hasMeta()) {
			TextureAtlasSpriteAccessor accessor = (TextureAtlasSpriteAccessor) sprite;
			AnimationMetadata metadata = accessor.getMetadata();

			int frameCount = accessor.getFrames().size();
			for (int frame = accessor.getFrame(); frame >= 0; frame--) {
				int frameIndex = metadata.getIndex(frame);
				if (frameIndex >= 0 && frameIndex < frameCount) {
                    net.minecraft.client.texture.TextureUtil.method_7027(accessor.getFrames().get(frameIndex), sprite.getWidth(), sprite.getHeight(), sprite.getX(), sprite.getY(), false, false);
                    return;
				}
			}
		}

		sprite.update();
	}

	public void cycleAnimationFrames() {
		GlStateManager.bindTexture(getGlId());
		for (Sprite sprite : animatedSprites) {
			sprite.update();
		}
	}

	public void close() {
		PBRAtlasHolder pbrHolder = ((TextureAtlasExtension) atlasTexture).getPBRHolder();
		if (pbrHolder != null) {
			switch (type) {
			case NORMAL:
				pbrHolder.setNormalAtlas(null);
				break;
			case SPECULAR:
				pbrHolder.setSpecularAtlas(null);
				break;
			}
		}
	}

	@Override
	public void load(ResourceManager manager) {
	}
}
