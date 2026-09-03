package org.clwn.interactivity.natives;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

public final class NativeLibraryLoader {

    private static final Set<String> LOADED = ConcurrentHashMap.newKeySet();

    private NativeLibraryLoader() {}

    public static void load(String... libraryNames) {
        for (String name : libraryNames) {
            load(name);
        }
    }

    public static synchronized void load(String libraryName) {
        if (!LOADED.add(libraryName)) {
            return;
        }

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean isArm = arch.contains("aarch64") || arch.contains("arm64");

        String platform;
        String fileName;

        if (os.contains("win")) {
            platform = isArm ? "windows-aarch64" : "windows-x86_64";
            fileName = libraryName + ".dll";
        } else if (os.contains("mac") || os.contains("darwin")) {
            platform = isArm ? "macos-aarch64" : "macos-x86_64";
            fileName = "lib" + libraryName + ".dylib";
        } else {
            platform = isArm ? "linux-aarch64" : "linux-x86_64";
            fileName = "lib" + libraryName + ".so";
        }

        try {
            String resourcePath = "/natives/" + platform + "/" + fileName;
            InputStream stream = NativeLibraryLoader.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("natives/" + platform + "/" + fileName);
            }
            if (stream == null) {
                LOADED.remove(libraryName);
                throw new UnsatisfiedLinkError("Native library not found in resources: " + resourcePath);
            }

            byte[] resourceBytes;
            try (InputStream in = stream) {
                resourceBytes = in.readAllBytes();
            }

            Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), "interactivity_natives", platform, fileName);
            Files.createDirectories(tempFile.getParent());

            // Extract if file does not exist, differs in size, or differs in CRC32 hash
            boolean needsExtract = !Files.exists(tempFile);
            if (!needsExtract) {
                try {
                    needsExtract = Files.size(tempFile) != resourceBytes.length
                            || crc32(resourceBytes) != crc32(Files.readAllBytes(tempFile));
                } catch (Exception ignored) {
                    needsExtract = true;
                }
            }

            if (needsExtract) {
                try {
                    Files.write(tempFile, resourceBytes);
                } catch (Exception e) {
                    // On Windows, if file is locked by a running process, reuse existing file if present
                    if (!Files.exists(tempFile)) {
                        throw e;
                    }
                }
            }

            // Invoking System.load() within this class binds JNI to this ClassLoader (required for NeoForge)
            System.load(tempFile.toAbsolutePath().toString());
        } catch (Throwable t) {
            LOADED.remove(libraryName);
            throw new UnsatisfiedLinkError("Failed to load native library '" + libraryName + "': " + t.getMessage());
        }
    }

    private static long crc32(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }
}
