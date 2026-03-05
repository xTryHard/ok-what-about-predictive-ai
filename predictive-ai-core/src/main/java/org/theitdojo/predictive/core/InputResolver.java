package org.theitdojo.predictive.core;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Resolves a user-supplied input string (local path or URL) to a local file path.
 * Downloads remote URLs to a temp file automatically.
 */
public final class InputResolver {

    private InputResolver() {}

    public record Resolved(Path path, boolean temporary) {}

    public static Resolved resolve(String input) throws Exception {
        return resolve(input, "input-");
    }

    public static Resolved resolve(String input, String tempPrefix) throws Exception {
        if (input.startsWith("http://") || input.startsWith("https://")) {
            String urlPath = URI.create(input).getPath();
            int dotIdx = urlPath.lastIndexOf('.');
            String suffix = (dotIdx >= 0 && dotIdx > urlPath.lastIndexOf('/')) ? urlPath.substring(dotIdx) : ".tmp";
            Path temp = Files.createTempFile(tempPrefix, suffix);
            System.out.println("Downloading image...");
            try (InputStream in = URI.create(input).toURL().openStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("Download complete.");
            return new Resolved(temp, true);
        }
        Path local = Paths.get(input).toAbsolutePath().normalize();
        if (!Files.exists(local)) throw new IllegalArgumentException("Input file not found: " + local);
        return new Resolved(local, false);
    }
}
