package com.prupe.mcpatcher.mal.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ZipResourcePack;

import com.prupe.mcpatcher.MCLogger;
import com.prupe.mcpatcher.MCPatcherUtils;
import net.minecraft.util.Identifier;

public class ResourceList {

    private static final MCLogger logger = MCLogger.getLogger(MCLogger.Category.TEXTURE_PACK);

    private static ResourceList instance;
    private static final Map<ResourcePack, Integer> resourcePackOrder = new WeakHashMap<>();

    private final ResourcePack resourcePack;
    private final Set<IdentifierWithSource> allResources = new TreeSet<>(
        new IdentifierWithSource.Comparator1());

    public static ResourceList getInstance() {
        if (instance == null) {
            List<ResourcePack> resourcePacks = TexturePackAPI.getResourcePacks(null);
            int order = resourcePacks.size();
            resourcePackOrder.clear();
            for (ResourcePack resourcePack : resourcePacks) {
                resourcePackOrder.put(resourcePack, order);
                order--;
            }
            instance = new ResourceList();
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    public static int getResourcePackOrder(ResourcePack resourcePack) {
        Integer i = resourcePackOrder.get(resourcePack);
        return i == null ? Integer.MAX_VALUE : i;
    }

    private ResourceList() {
        this.resourcePack = null;
        for (ResourcePack resourcePack : TexturePackAPI.getResourcePacks(null)) {
            ResourceList sublist;
            if (resourcePack instanceof ZipResourcePack) {
                sublist = new ResourceList((ZipResourcePack) resourcePack);
            } else if (resourcePack instanceof DefaultResourcePack) {
                sublist = new ResourceList((DefaultResourcePack) resourcePack);
            } else if (resourcePack instanceof AbstractFileResourcePack) {
                sublist = new ResourceList((AbstractFileResourcePack) resourcePack);
            } else {
                continue;
            }
            allResources.removeAll(sublist.allResources);
            allResources.addAll(sublist.allResources);
        }
        logger.fine("new %s", this);
        if (logger.isLoggable(Level.FINEST)) {
            for (IdentifierWithSource resource : allResources) {
                logger.finest(
                    "%s -> %s",
                    resource,
                    resource.getSource()
                        .getName());
            }
        }
    }

    private ResourceList(ZipResourcePack resourcePack) {
        this.resourcePack = resourcePack;
        scanZipFile(resourcePack.file);
        logger.fine("new %s", this);
    }

    private ResourceList(DefaultResourcePack resourcePack) {
        this.resourcePack = resourcePack;
        String version = "1.7.10";
        File jar = MCPatcherUtils.getMinecraftPath("versions", version, version + ".jar");
        if (jar.isFile()) {
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(jar);
                scanZipFile(zipFile);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                MCPatcherUtils.close(zipFile);
            }
        }
        Map<String, File> map = resourcePack.assetsIndex; // map
        if (map != null) {
            for (Map.Entry<String, File> entry : map.entrySet()) {
                String key = entry.getKey();
                File file = entry.getValue();
                Identifier resource = new Identifier(key);
                addResource(resource, file.isFile(), file.isDirectory());
            }
        }
        if (!allResources.isEmpty()) {
            logger.fine("new %s", this);
        }
    }

    private ResourceList(AbstractFileResourcePack resourcePack) {
        this.resourcePack = resourcePack;
        File directory = resourcePack.base;
        if (directory == null || !directory.isDirectory()) {
            return;
        }
        Set<String> allFiles = new HashSet<>();
        listAllFiles(directory, "", allFiles);
        for (String path : allFiles) {
            Identifier resource = TexturePackAPI.parsePath(path);
            if (resource != null) {
                File file = new File(directory, path);
                addResource(resource, file.isFile(), file.isDirectory());
            }
        }
        logger.fine("new %s", this);
    }

    private void scanZipFile(ZipFile zipFile) {
        if (zipFile == null) {
            return;
        }
        for (ZipEntry entry : Collections.list(zipFile.entries())) {
            String path = entry.getName();
            Identifier resource = TexturePackAPI.parsePath(path);
            if (resource != null) {
                addResource(resource, !entry.isDirectory(), entry.isDirectory());
            }
        }
    }

    private static void listAllFiles(File base, String subdir, Set<String> files) {
        File[] entries = new File(base, subdir).listFiles();
        if (entries == null) {
            return;
        }
        for (File file : entries) {
            String newPath = subdir + file.getName();
            if (files.add(newPath)) {
                if (file.isDirectory()) {
                    listAllFiles(base, subdir + file.getName() + '/', files);
                }
            }
        }
    }

    private void addResource(Identifier resource, boolean isFile, boolean isDirectory) {
        if (isFile) {
            allResources.add(new IdentifierWithSource(resourcePack, resource));
        } else if (isDirectory) {
            if (!resource.getPath()
                .endsWith("/")) {
                resource = new Identifier(resource.getNamespace(), resource.getPath() + '/');
            }
            allResources.add(new IdentifierWithSource(resourcePack, resource));
        }
    }

    public List<Identifier> listResources(String directory, String suffix, boolean sortByFilename) {
        return listResources(directory, suffix, true, false, sortByFilename);
    }

    public List<Identifier> listResources(String directory, String suffix, boolean recursive, boolean directories,
        boolean sortByFilename) {
        return listResources(null, directory, suffix, recursive, directories, sortByFilename);
    }

    public List<Identifier> listResources(String namespace, String directory, String suffix, boolean recursive,
        boolean directories, final boolean sortByFilename) {
        if (suffix == null) {
            suffix = "";
        }
        if (MCPatcherUtils.isNullOrEmpty(directory)) {
            directory = "";
        } else if (!directory.endsWith("/")) {
            directory += '/';
        }

        Set<IdentifierWithSource> tmpList = new TreeSet<>(
            new IdentifierWithSource.Comparator1(true, sortByFilename ? suffix : null));
        boolean allNamespaces = MCPatcherUtils.isNullOrEmpty(namespace);
        for (IdentifierWithSource resource : allResources) {
            if (directories != resource.isDirectory()) {
                continue;
            }
            if (!allNamespaces && !namespace.equals(resource.getNamespace())) {
                continue;
            }
            String path = resource.getPath();
            if (!path.endsWith(suffix)) {
                continue;
            }
            if (!path.startsWith(directory)) {
                continue;
            }
            if (!recursive) {
                String subpath = path.substring(directory.length());
                if (subpath.contains("/")) {
                    continue;
                }
            }
            tmpList.add(resource);
        }

        return new ArrayList<>(tmpList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ResourceList: ");
        if (resourcePack == null) {
            sb.append("(combined) ");
        } else {
            sb.append(resourcePack.getName())
                .append(' ');
        }
        int fileCount = 0;
        int directoryCount = 0;
        Set<String> namespaces = new HashSet<>();
        for (IdentifierWithSource resource : allResources) {
            if (resource.isDirectory()) {
                directoryCount++;
            } else {
                fileCount++;
            }
            namespaces.add(resource.getNamespace());
        }
        sb.append(fileCount)
            .append(" files, ");
        sb.append(directoryCount)
            .append(" directories in ");
        sb.append(namespaces.size())
            .append(" namespaces");
        return sb.toString();
    }
}
