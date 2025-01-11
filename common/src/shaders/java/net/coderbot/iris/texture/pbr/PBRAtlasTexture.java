package net.coderbot.iris.texture.pbr;

import com.mojang.blaze3d.platform.TextureUtil;
import net.coderbot.iris.mixin.texture.TextureAtlasSpriteAccessor;
import net.coderbot.iris.texture.util.TextureExporter;
import net.coderbot.iris.texture.util.TextureManipulationUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.util.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PBRAtlasTexture extends AbstractTexture {
	protected final TextureAtlas atlasTexture;
	protected final PBRType type;
	protected final Identifier id;
	protected final Map<Identifier, TextureAtlasSprite> sprites = new HashMap<>();
	protected final Set<TextureAtlasSprite> animatedSprites = new HashSet<>();

	public PBRAtlasTexture(TextureAtlas atlasTexture, PBRType type) {
		this.atlasTexture = atlasTexture;
		this.type = type;
		id = type.appendToFileLocation(atlasTexture.location());
	}

	public PBRType getType() {
		return type;
	}

	public Identifier getAtlasId() {
		return id;
	}

	public void addSprite(TextureAtlasSprite sprite) {
		sprites.put(sprite.getName(), sprite);
		if (sprite.isAnimation()) {
			animatedSprites.add(sprite);
		}
	}

	@Nullable
	public TextureAtlasSprite getSprite(Identifier id) {
		return sprites.get(id);
	}

	public void clear() {
		sprites.clear();
		animatedSprites.clear();
	}

	public void upload(int atlasWidth, int atlasHeight, int mipLevel) {
		int glId = getId();
		TextureUtil.prepareImage(glId, mipLevel, atlasWidth, atlasHeight);
		TextureManipulationUtil.fillWithColor(glId, mipLevel, type.getDefaultValue());

		for (TextureAtlasSprite sprite : sprites.values()) {
			try {
				uploadSprite(sprite);
			} catch (Throwable throwable) {
				CrashReport crashReport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
				crashReportCategory.setDetail("Atlas path", id);
				crashReportCategory.setDetail("Sprite", sprite);
				throw new ReportedException(crashReport);
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

	protected void uploadSprite(TextureAtlasSprite sprite) {
		if (sprite.isAnimation()) {
			TextureAtlasSpriteAccessor accessor = (TextureAtlasSpriteAccessor) sprite;
			AnimationMetadataSection metadata = accessor.getMetadata();

			int frameCount = sprite.getFrameCount();
			for (int frame = accessor.getFrame(); frame >= 0; frame--) {
				int frameIndex = metadata.getFrameIndex(frame);
				if (frameIndex >= 0 && frameIndex < frameCount) {
					accessor.callUpload(frameIndex);
					return;
				}
			}
		}

		sprite.uploadFirstFrame();
	}

	public void cycleAnimationFrames() {
		bind();
		for (TextureAtlasSprite sprite : animatedSprites) {
			sprite.cycleFrames();
		}
	}

	@Override
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
