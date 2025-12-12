package com.prupe.mcpatcher.mal.resource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.prupe.mcpatcher.core.AbstractTextureExpansion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.FallbackResourceManager;
import net.minecraft.client.texture.*;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

import org.apache.commons.io.IOUtils;

import com.prupe.mcpatcher.MCLogger;
import com.prupe.mcpatcher.MCPatcherUtils;

public class TexturePackAPI {

    private static final MCLogger logger = MCLogger.getLogger(MCLogger.Category.TEXTURE_PACK);

    public static final String DEFAULT_NAMESPACE = "minecraft";

    public static final String MCPATCHER_SUBDIR = "mcpatcher/";
    public static final Identifier ITEMS_PNG = new Identifier("textures/atlas/items.png");

    private static final String ASSETS = "assets/";

    public static List<ResourcePack> getResourcePacks(String namespace) {
        List<ResourcePack> resourcePacks = new ArrayList<>();
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        if (resourceManager instanceof ReloadableResourceManagerImpl) {
            Set<Map.Entry<String, FallbackResourceManager>> entrySet = ((ReloadableResourceManagerImpl) resourceManager).fallbackManagers
                .entrySet();
            for (Map.Entry<String, FallbackResourceManager> entry : entrySet) {
                if (namespace == null || namespace.equals(entry.getKey())) {
                    List<ResourcePack> packs = entry.getValue().resourcePacks;
                    if (packs != null) {
                        resourcePacks.removeAll(packs);
                        resourcePacks.addAll(packs);
                    }
                }
            }
        }
        return resourcePacks;
    }

    public static Set<String> getNamespaces() {
        Set<String> namespaces = new HashSet<>();
        namespaces.add(DEFAULT_NAMESPACE);
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        if (resourceManager instanceof ReloadableResourceManagerImpl simpleReloadableResourceManager) {
            namespaces.addAll(simpleReloadableResourceManager.fallbackManagers.keySet());
        }
        return namespaces;
    }

    public static boolean isDefaultTexturePack() {
        return getResourcePacks(DEFAULT_NAMESPACE).size() <= 1;
    }

    public static InputStream getInputStream(Identifier resource) {
        try {
            if (resource instanceof IdentifierWithSource identifierWithSource) {
                try {
                    return identifierWithSource.getSource()
                            .open(resource);
                } catch (IOException e) {}
            }
            return resource == null ? null
                : MinecraftClient.getInstance()
                    .getResourceManager()
                    .getResource(resource)
                    .getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean hasResource(Identifier resource) {
        if (resource == null) {
            return false;
        } else if (resource.getPath()
            .endsWith(".png")) {
                return getImage(resource) != null;
            } else if (resource.getPath()
                .endsWith(".properties")) {
                    return getProperties(resource) != null;
                } else {
                    InputStream is = getInputStream(resource);
                    MCPatcherUtils.close(is);
                    return is != null;
                }
    }

    public static boolean hasCustomResource(Identifier resource) {
        InputStream jar = null;
        InputStream pack = null;
        try {
            pack = getInputStream(resource);
            jar = MinecraftClient.getInstance().defaultResourcePack.open(resource);

            if (pack == null || jar == null) {
                return false;
            }
            return !Arrays.equals(IOUtils.toByteArray(jar), IOUtils.toByteArray(pack));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            MCPatcherUtils.close(jar);
            MCPatcherUtils.close(pack);
        }
        return false;
    }

    public static BufferedImage getImage(Identifier resource) {
        if (resource == null) {
            return null;
        }
        InputStream input = getInputStream(resource);
        BufferedImage image = null;
        if (input != null) {
            try {
                image = ImageIO.read(input);
            } catch (IOException e) {
                logger.error("could not read %s", resource);
                e.printStackTrace();
            } finally {
                MCPatcherUtils.close(input);
            }
        }
        return image;
    }

    public static Properties getProperties(Identifier resource) {
        Properties properties = new Properties();
        if (getProperties(resource, properties)) {
            return properties;
        } else {
            return null;
        }
    }

    public static boolean getProperties(Identifier resource, Properties properties) {
        if (properties != null) {
            InputStream input = getInputStream(resource);
            try {
                if (input != null) {
                    properties.load(input);
                    return true;
                }
            } catch (IOException e) {
                logger.error("could not read %s", resource);
                e.printStackTrace();
            } finally {
                MCPatcherUtils.close(input);
            }
        }
        return false;
    }

    public static Identifier transformResourceLocation(Identifier resource, String oldExt, String newExt) {
        return new Identifier(
            resource.getNamespace(),
            resource.getPath()
                .replaceFirst(Pattern.quote(oldExt) + "$", newExt));
    }

    public static Identifier parsePath(String path) {
        if (MCPatcherUtils.isNullOrEmpty(path)) {
            return null;
        }
        path = path.replace(File.separatorChar, '/');
        if (path.startsWith(ASSETS)) {
            path = path.substring(ASSETS.length());
            int slash = path.indexOf('/');
            if (slash > 0 && slash + 1 < path.length()) {
                return new Identifier(path.substring(0, slash), path.substring(slash + 1));
            }
        }
        return null;
    }

    public static Identifier parseIdentifier(Identifier baseResource, String path) {
        if (MCPatcherUtils.isNullOrEmpty(path)) {
            return null;
        }
        boolean absolute = false;
        if (path.startsWith("%blur%")) {
            path = path.substring(6);
        }
        if (path.startsWith("%clamp%")) {
            path = path.substring(7);
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
            absolute = true;
        }
        if (path.startsWith("assets/minecraft/")) {
            path = path.substring(17);
            absolute = true;
        }
        // Absolute path, including namespace:
        // namespace:path/filename -> assets/namespace/path/filename
        int colon = path.indexOf(':');
        if (colon >= 0) {
            return new Identifier(path.substring(0, colon), path.substring(colon + 1));
        }
        Identifier resource;
        if (path.startsWith("~/")) {
            // Relative to namespace mcpatcher dir:
            // ~/path -> assets/(namespace of base file)/mcpatcher/path
            resource = new Identifier(baseResource.getNamespace(), MCPATCHER_SUBDIR + path.substring(2));
        } else if (path.startsWith("./")) {
            // Relative to properties file:
            // ./path -> (dir of base file)/path
            resource = new Identifier(
                baseResource.getNamespace(),
                baseResource.getPath()
                    .replaceFirst("[^/]+$", "") + path.substring(2));
        } else if (!absolute && !path.contains("/")) {
            // Relative to properties file:
            // filename -> (dir of base file)/filename
            resource = new Identifier(
                baseResource.getNamespace(),
                baseResource.getPath()
                    .replaceFirst("[^/]+$", "") + path);
        } else {
            // Absolute path, w/o namespace:
            // path/filename -> assets/(namespace of base file)/path/filename
            resource = new Identifier(baseResource.getNamespace(), path);
        }
        if (baseResource instanceof IdentifierWithSource) {
            resource = new IdentifierWithSource(
                ((IdentifierWithSource) baseResource).getSource(),
                resource);
        }
        return resource;
    }

    public static Identifier newMCPatcherResourceLocation(String path) {
        return new Identifier(MCPATCHER_SUBDIR + path.replaceFirst("^/+", ""));
    }

    public static int getTextureIfLoaded(Identifier resource) {
        if (resource == null) {
            return -1;
        }
        Texture texture = MinecraftClient.getInstance()
            .getTextureManager()
            .getTexture(resource);
        return texture instanceof AbstractTexture ? texture.getGlId() : -1;
    }

    public static boolean isTextureLoaded(Identifier resource) {
        return getTextureIfLoaded(resource) >= 0;
    }

    public static Texture getTextureObject(Identifier resource) {
        return MinecraftClient.getInstance()
                .getTextureManager()
                .getTexture(resource);
    }

    public static void bindTexture(Identifier resource) {
        if (resource != null) {
            MinecraftClient.getInstance()
                .getTextureManager()
                .bindTexture(resource);
        }
    }

    public static void unloadTexture(Identifier resource) {
        if (resource != null) {
            TextureManager textureManager = MinecraftClient.getInstance()
                .getTextureManager();
            Texture texture = textureManager.getTexture(resource);
            if (texture != null && !(texture instanceof SpriteAtlasTexture) && !(texture instanceof NativeImageBackedTexture)) {
                if (texture instanceof AbstractTexture) {
                    ((AbstractTextureExpansion) texture).unloadGLTexture();
                }
                logger.finer("unloading texture %s", resource);
                textureManager.textures.remove(resource);
            }
        }
    }

    public static void flushUnusedTextures() {

    }

}
