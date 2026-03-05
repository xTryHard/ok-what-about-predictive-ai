package org.theitdojo.predictive.djl.detector;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads an object detection model and runs inference.
 * Use the static factory to get a pre-configured detector.
 */
public class ObjectDetector implements AutoCloseable {

    private final ZooModel<Image, DetectedObjects> model;
    private final Predictor<Image, DetectedObjects> predictor;

    private ObjectDetector(Criteria<Image, DetectedObjects> criteria) throws Exception {
        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();
    }

    /**
     * Returns a detector configured for the bundled bat detection model.
     * The model zip is resolved from a "models/" directory next to the running JAR,
     * or from the working directory if running via mvn exec:java.
     */
    public static ObjectDetector forBats() throws Exception {
        // Locate the directory containing this class's JAR (or target/classes when
        // running with mvn exec:java) and look for models/ next to it.
        URI location = ObjectDetector.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI();
        Path jarDir = Paths.get(location).getParent();
        Path modelDir = jarDir.resolve("models");

        // Fallback: look in predictive-ai-djl/target/models/ (covers running
        // from the root project or from predictive-ai-demos via exec:java, where
        // the class may be loaded from the .m2 repository JAR).
        if (!modelDir.resolve("bat_tracking.onnx").toFile().exists()) {
            modelDir = Paths.get("predictive-ai-djl/target/models");
        }

        // Final fallback: target/models relative to CWD (standalone execution).
        if (!modelDir.resolve("bat_tracking.onnx").toFile().exists()) {
            modelDir = Paths.get("target/models");
        }

        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optModelPath(modelDir)
                .optModelName("bat_tracking")
                .optTranslator(new BatDetectorTranslator())
                .optEngine("OnnxRuntime")
                .build();
        return new ObjectDetector(criteria);
    }

    /**
     * Returns a detector configured for the bundled baseball detection model (YOLOv11).
     */
    public static ObjectDetector forBaseball() throws Exception {
        URI location = ObjectDetector.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI();
        Path jarDir = Paths.get(location).getParent();
        Path modelDir = jarDir.resolve("models");

        if (!modelDir.resolve("ball_tracking_v4-YOLOv11.onnx").toFile().exists()) {
            modelDir = Paths.get("predictive-ai-djl/target/models");
        }

        if (!modelDir.resolve("ball_tracking_v4-YOLOv11.onnx").toFile().exists()) {
            modelDir = Paths.get("target/models");
        }

        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optModelPath(modelDir)
                .optModelName("ball_tracking_v4-YOLOv11")
                .optTranslator(new BaseballDetectorTranslator())
                .optEngine("OnnxRuntime")
                .build();
        return new ObjectDetector(criteria);
    }

    public DetectedObjects detect(Image image) throws Exception {
        return predictor.predict(image);
    }

    @Override
    public void close() {
        predictor.close();
        model.close();
    }
}
