package gg.sona.radium.mcpatcher.mal.resource;

import java.util.Comparator;
import java.util.regex.Pattern;

import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

import gg.sona.radium.mcpatcher.MCPatcherUtils;

public class IdentifierWithSource extends Identifier {

    private final ResourcePack source;
    private final int order;
    private final boolean isDirectory;

    public IdentifierWithSource(ResourcePack source, Identifier resource) {
        super(
            resource.getNamespace(),
            resource.getPath()
                .replaceFirst("/$", ""));
        this.source = source;
        order = ResourceList.getResourcePackOrder(source);
        isDirectory = resource.getPath()
            .endsWith("/");
    }

    public ResourcePack getSource() {
        return source;
    }

    public int getOrder() {
        return order;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    static class Comparator1 implements Comparator<IdentifierWithSource> {

        private final boolean bySource;
        private final String suffixExpr;

        Comparator1() {
            this(false, null);
        }

        Comparator1(boolean bySource, String suffix) {
            this.bySource = bySource;
            this.suffixExpr = MCPatcherUtils.isNullOrEmpty(suffix) ? null : Pattern.quote(suffix) + "$";
        }

        @Override
        public int compare(IdentifierWithSource o1, IdentifierWithSource o2) {
            int result;
            if (bySource) {
                result = o1.getOrder() - o2.getOrder();
                if (result != 0) {
                    return result;
                }
            }
            String n1 = o1.getNamespace();
            String n2 = o2.getNamespace();
            result = n1.compareTo(n2);
            if (result != 0) {
                return result;
            }
            String p1 = o1.getPath();
            String p2 = o2.getPath();
            if (suffixExpr != null) {
                String f1 = p1.replaceAll(".*/", "")
                    .replaceFirst(suffixExpr, "");
                String f2 = p2.replaceAll(".*/", "")
                    .replaceFirst(suffixExpr, "");
                result = f1.compareTo(f2);
                if (result != 0) {
                    return result;
                }
            }
            return p1.compareTo(p2);
        }
    }
}
