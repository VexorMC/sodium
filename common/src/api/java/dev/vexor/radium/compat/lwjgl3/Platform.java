package dev.vexor.radium.compat.lwjgl3;

import java.util.regex.*;

/** The platforms supported by LWJGL. */
public enum Platform {

    FREEBSD("FreeBSD", "freebsd") {
        private final Pattern SO = Pattern.compile("(?:^|/)lib\\w+[.]so(?:[.]\\d+)*$");

        @Override
        String mapLibraryName(String name) {
            if (SO.matcher(name).find()) {
                return name;
            }

            return System.mapLibraryName(name);
        }
    },
    LINUX("Linux", "linux") {
        private final Pattern SO = Pattern.compile("(?:^|/)lib\\w+[.]so(?:[.]\\d+)*$");

        @Override
        String mapLibraryName(String name) {
            if (SO.matcher(name).find()) {
                return name;
            }

            return System.mapLibraryName(name);
        }
    },
    // TODO: Rename to MACOS in LWJGL 4
    MACOSX("macOS", "macos") {
        private final Pattern DYLIB = Pattern.compile("(?:^|/)lib\\w+(?:[.]\\d+)*[.]dylib$");

        @Override
        String mapLibraryName(String name) {
            if (DYLIB.matcher(name).find()) {
                return name;
            }

            return System.mapLibraryName(name);
        }
    },
    WINDOWS("Windows", "windows") {
        @Override
        String mapLibraryName(String name) {
            if (name.endsWith(".dll")) {
                return name;
            }

            return System.mapLibraryName(name);
        }
    };

    /** The architectures supported by LWJGL. */
    public enum Architecture {
        X64(true),
        X86(false),
        ARM64(true),
        ARM32(false),
        PPC64LE(true),
        RISCV64(true);

        static final Architecture current;

        final boolean is64Bit;

        static {
            String  osArch  = System.getProperty("os.arch");
            boolean is64Bit = osArch.contains("64") || osArch.startsWith("armv8");

            if (osArch.startsWith("arm") || osArch.startsWith("aarch")) {
                current = is64Bit ? Architecture.ARM64 : Architecture.ARM32;
            } else if (osArch.startsWith("ppc")) {
                if (!"ppc64le".equals(osArch)) {
                    throw new UnsupportedOperationException("Only PowerPC 64 LE is supported.");
                }
                current = Architecture.PPC64LE;
            } else if (osArch.startsWith("riscv")) {
                if (!"riscv64".equals(osArch)) {
                    throw new UnsupportedOperationException("Only RISC-V 64 is supported.");
                }
                current = Architecture.RISCV64;
            } else {
                current = is64Bit ? Architecture.X64 : Architecture.X86;
            }
        }

        Architecture(boolean is64Bit) {
            this.is64Bit = is64Bit;
        }
    }

    private static final Platform current;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            current = WINDOWS;
        } else if (osName.startsWith("FreeBSD")) {
            current = FREEBSD;
        } else if (osName.startsWith("Linux") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
            current = LINUX;
        } else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
            current = MACOSX;
        } else {
            throw new LinkageError("Unknown platform: " + osName);
        }

    }

    private final String name;
    private final String nativePath;

    Platform(String name, String nativePath) {
        this.name = name;
        this.nativePath = nativePath;
    }

    /** Returns the platform name. */
    public String getName() {
        return name;
    }

    abstract String mapLibraryName(String name);

    /** Returns the platform on which the library is running. */
    public static Platform get() {
        return current;
    }

    /** Returns the architecture on which the library is running. */
    public static Architecture getArchitecture() {
        return Architecture.current;
    }
}
