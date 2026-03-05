package org.theitdojo.predictive.tensorflow;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;

/**
 * Resolves the EfficientDet SavedModel directory to a real filesystem path.
 * When running from an exploded classpath (target/classes), returns the path directly.
 * When running from a JAR, extracts the model to a temporary directory.
 */
final class ModelResolver {

    private static final String MODEL_RESOURCE = "/models/efficientdet";
    private static final String MARKER_FILE = "saved_model.pb";

    private ModelResolver() {}

    static String resolveModelDir() {
        var resource = ModelResolver.class.getResource(MODEL_RESOURCE + "/" + MARKER_FILE);
        if (resource != null) {
            try {
                URI uri = resource.toURI();
                if ("file".equals(uri.getScheme())) {
                    // Running from exploded classes — use the directory directly
                    return Paths.get(uri).getParent().toString();
                }
                if ("jar".equals(uri.getScheme())) {
                    return extractModelFromJar();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to resolve model from classpath resource", e);
            }
        }

        // Fallback: relative to CWD
        Path fallback = Paths.get("predictive-ai-tensorflow/src/main/resources/models/efficientdet");
        if (Files.exists(fallback.resolve(MARKER_FILE))) {
            return fallback.toAbsolutePath().toString();
        }

        throw new IllegalStateException(
                "Cannot locate EfficientDet SavedModel. Ensure saved_model.pb is in resources/models/efficientdet/");
    }

    private static String extractModelFromJar() throws Exception {
        Path tempDir = Files.createTempDirectory("efficientdet-model-");
        tempDir.toFile().deleteOnExit();

        // Walk the model resource tree inside the JAR and copy each file
        URI jarUri = ModelResolver.class.getResource(MODEL_RESOURCE).toURI();
        // jarUri looks like jar:file:/path/to.jar!/models/efficientdet
        // We need to open the JAR filesystem to walk the directory
        String[] parts = jarUri.toString().split("!");
        try (FileSystem jarFs = FileSystems.newFileSystem(URI.create(parts[0]), Collections.emptyMap())) {
            Path modelRoot = jarFs.getPath(MODEL_RESOURCE);
            try (var stream = Files.walk(modelRoot)) {
                stream.forEach(source -> {
                    try {
                        Path dest = tempDir.resolve(modelRoot.relativize(source).toString());
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.createDirectories(dest.getParent());
                            try (InputStream in = Files.newInputStream(source)) {
                                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }

        return tempDir.toString();
    }
}
