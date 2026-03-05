package org.theitdojo.predictive.djl.processor;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import org.theitdojo.predictive.core.InputResolver;
import org.theitdojo.predictive.djl.detector.ObjectDetector;
import org.theitdojo.predictive.djl.renderer.DetectionRenderer;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageProcessor implements MediaProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessor.class);

    private final ObjectDetector detector;

    public ImageProcessor(ObjectDetector detector) {
        this.detector = detector;
    }

    @Override
    public void process(String input, Path output) throws Exception {
        InputResolver.Resolved resolved = InputResolver.resolve(input, "bat-input-");
        logger.info("Resolved input {} -> {}", input, resolved.path());

        try {
            Image img = ImageFactory.getInstance().fromFile(resolved.path());
            logger.info("Running detection on {}", input);

            DetectedObjects detections = detector.detect(img);
            logger.info("Detections: {}", detections);

            printResults(detections);

            DetectionRenderer.draw(img, detections);

            String format = output.getFileName().toString().matches(".*\\.jpe?g") ? "jpg" : "png";
            try (OutputStream os = Files.newOutputStream(output)) {
                img.save(os, format);
            }
            logger.info("Result saved to {}", output);
        } finally {
            if (resolved.temporary()) Files.deleteIfExists(resolved.path());
        }
    }

    private void printResults(DetectedObjects detections) {
        List<DetectedObject> items = detections.items();
        DecimalFormat pct = new DecimalFormat("#.##");

        System.out.println("--- Detection Results ---");
        System.out.println("Objects detected: " + items.size());

        for (int i = 0; i < items.size(); i++) {
            DetectedObject item = items.get(i);
            String confidence = pct.format(item.getProbability() * 100) + "%";
            System.out.println("  " + (i + 1) + ") " + item.getClassName() + " — confidence: " + confidence);
        }

        if (items.isEmpty()) {
            System.out.println("  No objects detected. Try a different image or lower the confidence threshold.");
        }

        System.out.println("-------------------------");
    }
}
