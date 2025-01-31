package net.coderbot.iris.texture.pbr.loader;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import net.coderbot.iris.Iris;
import net.coderbot.iris.mixin.texture.AnimationMetadataSectionAccessor;
import net.coderbot.iris.mixin.texture.TextureAtlasAccessor;
import net.coderbot.iris.mixin.texture.TextureAtlasSpriteAccessor;
import net.coderbot.iris.texture.TextureInfoCache;
import net.coderbot.iris.texture.TextureInfoCache.TextureInfo;
import net.coderbot.iris.texture.format.TextureFormat;
import net.coderbot.iris.texture.format.TextureFormatLoader;
import net.coderbot.iris.texture.mipmap.ChannelMipmapGenerator;
import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.coderbot.iris.texture.mipmap.LinearBlendFunction;
import net.coderbot.iris.texture.pbr.PBRAtlasTexture;
import net.coderbot.iris.texture.pbr.PBRSpriteHolder;
import net.coderbot.iris.texture.pbr.PBRType;
import net.coderbot.iris.texture.pbr.TextureAtlasSpriteExtension;
import net.coderbot.iris.texture.util.ImageManipulationUtil;
import net.minecraft.client.resource.AnimationMetadata;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;

public class AtlasPBRLoader implements PBRTextureLoader<SpriteAtlasTexture> {
	public static final ChannelMipmapGenerator LINEAR_MIPMAP_GENERATOR = new ChannelMipmapGenerator(
			LinearBlendFunction.INSTANCE,
			LinearBlendFunction.INSTANCE,
			LinearBlendFunction.INSTANCE,
			LinearBlendFunction.INSTANCE
	);
    public static final AnimationMetadata EMPTY_ANIMATION_METADATA = new AnimationMetadata(Lists.newArrayList(), -1, -1, -1, false);

	@Override
	public void load(SpriteAtlasTexture atlas, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer) {
		TextureInfo textureInfo = TextureInfoCache.INSTANCE.getInfo(atlas.getGlId());
		int atlasWidth = textureInfo.getWidth();
		int atlasHeight = textureInfo.getHeight();
		int mipLevel = fetchAtlasMipLevel(atlas);

		PBRAtlasTexture normalAtlas = null;
		PBRAtlasTexture specularAtlas = null;
		for (Sprite sprite : ((TextureAtlasAccessor) atlas).getTexturesByName().values()) {
			if (!sprite.getName().equals(SpriteAtlasTexture.MISSING.toString())) {
				Sprite normalSprite = createPBRSprite(sprite, resourceManager, atlas, atlasWidth, atlasHeight, mipLevel, PBRType.NORMAL);
                Sprite specularSprite = createPBRSprite(sprite, resourceManager, atlas, atlasWidth, atlasHeight, mipLevel, PBRType.SPECULAR);
				if (normalSprite != null) {
					if (normalAtlas == null) {
						normalAtlas = new PBRAtlasTexture(atlas, PBRType.NORMAL);
					}
					normalAtlas.addSprite(normalSprite);
					PBRSpriteHolder pbrSpriteHolder = ((TextureAtlasSpriteExtension) sprite).getOrCreatePBRHolder();
					pbrSpriteHolder.setNormalSprite(normalSprite);
				}
				if (specularSprite != null) {
					if (specularAtlas == null) {
						specularAtlas = new PBRAtlasTexture(atlas, PBRType.SPECULAR);
					}
					specularAtlas.addSprite(specularSprite);
					PBRSpriteHolder pbrSpriteHolder = ((TextureAtlasSpriteExtension) sprite).getOrCreatePBRHolder();
					pbrSpriteHolder.setSpecularSprite(specularSprite);
				}
			}
		}

		if (normalAtlas != null) {
			if (normalAtlas.tryUpload(atlasWidth, atlasHeight, mipLevel)) {
				pbrTextureConsumer.acceptNormalTexture(normalAtlas);
			}
		}
		if (specularAtlas != null) {
			if (specularAtlas.tryUpload(atlasWidth, atlasHeight, mipLevel)) {
				pbrTextureConsumer.acceptSpecularTexture(specularAtlas);
			}
		}
	}

	protected static int fetchAtlasMipLevel(SpriteAtlasTexture atlas) {
		Sprite missingSprite = atlas.getSprite(SpriteAtlasTexture.MISSING);
		return ((TextureAtlasSpriteAccessor) missingSprite).getMainImage().length - 1;
	}

	@Nullable
	protected Sprite createPBRSprite(Sprite sprite, ResourceManager resourceManager, SpriteAtlasTexture atlas, int atlasWidth, int atlasHeight, int mipLevel, PBRType pbrType) {
		Identifier spriteName = new Identifier(sprite.getName());
		Identifier imageLocation = ((TextureAtlasAccessor) atlas).completeResourceLocation(spriteName, 0);
		Identifier pbrImageLocation = pbrType.appendToFileLocation(imageLocation);

		Sprite pbrSprite = null;
		try {
            Resource resource = resourceManager.getResource(pbrImageLocation);
			NativeImage nativeImage = NativeImage.read(resource.getInputStream());
			AnimationMetadata animationMetadata = resource.getMetadata("animation");
			if (animationMetadata == null) {
				animationMetadata = new AnimationMetadata(Lists.newArrayList(), -1, -1, -1, false);
			}

			Pair<Integer, Integer> frameSize = getFrameSize(nativeImage.getWidth(), nativeImage.getHeight(), animationMetadata);
			int frameWidth = frameSize.getLeft();
			int frameHeight = frameSize.getRight();
			int targetFrameWidth = sprite.getWidth();
			int targetFrameHeight = sprite.getHeight();
			if (frameWidth != targetFrameWidth || frameHeight != targetFrameHeight) {
				int imageWidth = nativeImage.getWidth();
				int imageHeight = nativeImage.getHeight();

				// We can assume the following is always true as a result of getFrameSize's check:
				// imageWidth % frameWidth == 0 && imageHeight % frameHeight == 0
				int targetImageWidth = imageWidth / frameWidth * targetFrameWidth;
				int targetImageHeight = imageHeight / frameHeight * targetFrameHeight;

				NativeImage scaledImage;
				if (targetImageWidth % imageWidth == 0 && targetImageHeight % imageHeight == 0) {
					scaledImage = ImageManipulationUtil.scaleNearestNeighbor(nativeImage, targetImageWidth, targetImageHeight);
				} else {
					scaledImage = ImageManipulationUtil.scaleBilinear(nativeImage, targetImageWidth, targetImageHeight);
				}
				nativeImage.close();
				nativeImage = scaledImage;

				frameWidth = targetFrameWidth;
				frameHeight = targetFrameHeight;

				if (!animationMetadata.equals(EMPTY_ANIMATION_METADATA)) {
					AnimationMetadataSectionAccessor animationAccessor = (AnimationMetadataSectionAccessor) animationMetadata;
					int internalFrameWidth = animationAccessor.getFrameWidth();
					int internalFrameHeight = animationAccessor.getFrameHeight();
					if (internalFrameWidth != -1) {
						animationAccessor.setFrameWidth(frameWidth);
					}
					if (internalFrameHeight != -1) {
						animationAccessor.setFrameHeight(frameHeight);
					}
				}
			}

			Identifier pbrSpriteName = new Identifier(spriteName.getNamespace(), spriteName.getPath() + pbrType.getSuffix());
			SpriteInfo pbrSpriteInfo = new PBRTextureAtlasSpriteInfo(pbrSpriteName, frameWidth, frameHeight, animationMetadata, pbrType);

			int x = ((TextureAtlasSpriteAccessor) sprite).getX();
			int y = ((TextureAtlasSpriteAccessor) sprite).getY();
			pbrSprite = new PBRTextureAtlasSprite(pbrSpriteInfo, mipLevel, atlasWidth, atlasHeight, x, y, nativeImage);
			syncAnimation(sprite, pbrSprite);
		} catch (FileNotFoundException e) {
			//
		} catch (RuntimeException e) {
			Iris.logger.error("Unable to parse metadata from {} : {}", pbrImageLocation, e);
		} catch (IOException e) {
			Iris.logger.error("Unable to load {} : {}", pbrImageLocation, e);
		}

		return pbrSprite;
	}

    private Pair<Integer, Integer> getFrameSize(int i, int j, AnimationMetadata animationMetadataSection) {
        final Pair<Integer, Integer> pair = this.calculateFrameSize(i, j, animationMetadataSection);
        final int k = pair.getLeft();
        final int l = pair.getRight();
        if (isDivisionInteger(i, k) && isDivisionInteger(j, l)) {
            return pair;
        } else {
            throw new IllegalArgumentException(String.format("Image size %s,%s is not multiply of frame size %s,%s", i, j, k, l));
        }
    }

    private Pair<Integer, Integer> calculateFrameSize(int i, int j, AnimationMetadata animationMetadataSection) {
        if (animationMetadataSection.getWidth() != -1) {
            return animationMetadataSection.getHeight() != -1 ? new Pair<>(animationMetadataSection.getWidth(), animationMetadataSection.getHeight()) : new Pair<>(animationMetadataSection.getWidth(), j);
        } else if (animationMetadataSection.getHeight() != -1) {
            return new Pair<>(i, animationMetadataSection.getHeight());
        } else {
            int k = Math.min(i, j);
            return new Pair<>(k, k);
        }
    }

    private static boolean isDivisionInteger(int i, int j) {
        return i / j * j == i;
    }

	protected void syncAnimation(Sprite source, Sprite target) {
		if (!source.hasMeta() || !target.hasMeta()) {
			return;
		}

		TextureAtlasSpriteAccessor sourceAccessor = ((TextureAtlasSpriteAccessor) source);
		AnimationMetadata sourceMetadata = sourceAccessor.getMetadata();

		int ticks = 0;
		for (int f = 0; f < sourceAccessor.getFrame(); f++) {
			ticks += sourceMetadata.getTime(f);
		}

		TextureAtlasSpriteAccessor targetAccessor = ((TextureAtlasSpriteAccessor) target);
        AnimationMetadata targetMetadata = targetAccessor.getMetadata();

		int cycleTime = 0;
		int frameCount = targetAccessor.getFrames().size();
		for (int f = 0; f < frameCount; f++) {
			cycleTime += targetMetadata.getTime(f);
		}
		ticks %= cycleTime;

		int targetFrame = 0;
		while (true) {
			int time = targetMetadata.getTime(targetFrame);
			if (ticks >= time) {
				targetFrame++;
				ticks -= time;
			} else {
				break;
			}
		}

		targetAccessor.setFrame(targetFrame);
	}

	protected static class PBRTextureAtlasSpriteInfo extends SpriteInfo {
		protected final PBRType pbrType;

		public PBRTextureAtlasSpriteInfo(Identifier name, int width, int height, AnimationMetadata metadata, PBRType pbrType) {
			super(name, width, height, metadata);
			this.pbrType = pbrType;
		}
	}

	public static class PBRTextureAtlasSprite extends Sprite implements CustomMipmapGenerator.Provider {
		protected PBRTextureAtlasSprite(SpriteInfo info, int mipLevel, int atlasWidth, int atlasHeight, int x, int y, NativeImage nativeImage) {
			super(info.name().toString());
            super.reInitialize(atlasWidth, atlasHeight, x, y, false);
		}

		@Override
		public CustomMipmapGenerator getMipmapGenerator(SpriteInfo info, int atlasWidth, int atlasHeight) {
			if (info instanceof PBRTextureAtlasSpriteInfo) {
				PBRType pbrType = ((PBRTextureAtlasSpriteInfo) info).pbrType;
				TextureFormat format = TextureFormatLoader.getFormat();
				if (format != null) {
					CustomMipmapGenerator generator = format.getMipmapGenerator(pbrType);
					if (generator != null) {
						return generator;
					}
				}
			}
			return LINEAR_MIPMAP_GENERATOR;
		}
	}
}
