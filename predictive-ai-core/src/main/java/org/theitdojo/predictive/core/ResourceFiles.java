package org.theitdojo.predictive.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility for loading classpath resources as real files (needed by libraries that require Path).
 */
public final class ResourceFiles {
    private ResourceFiles() {}

    public static Path copyToTempFile(String classpathResource, String suffix) throws IOException {
        String res = classpathResource.startsWith("/") ? classpathResource : "/" + classpathResource;
        try (InputStream in = ResourceFiles.class.getResourceAsStream(res)) {
            if (in == null) {
                throw new IOException("Resource not found on classpath: " + res);
            }
            Path tmp = Files.createTempFile("predictive-ai-", suffix);
            tmp.toFile().deleteOnExit();
            Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }
    }
}
