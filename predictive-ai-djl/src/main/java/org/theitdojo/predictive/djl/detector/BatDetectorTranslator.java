package org.theitdojo.predictive.djl.detector;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.NoBatchifyTranslator;
import ai.djl.translate.TranslatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DJL Translator that handles YOLO pre-processing (letterbox) and
 * post-processing (confidence filtering + NMS) for the bat detection model.
 */
class BatDetectorTranslator implements NoBatchifyTranslator<Image, DetectedObjects> {

    private static final List<String> CLASSES = Arrays.asList("bat");
    private static final float THRESHOLD = 0.5f;
    private static final float IOU_THRESHOLD = 0.3f;
    private static final int INPUT_SIZE = 640;

    // Letterbox state — set during processInput, read during processOutput
    private int origW, origH;
    private float padW, padH, scale;

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        origW = input.getWidth();
        origH = input.getHeight();
        scale = Math.min((float) INPUT_SIZE / origH, (float) INPUT_SIZE / origW);
        int newW = Math.round(origW * scale);
        int newH = Math.round(origH * scale);
        padW = (INPUT_SIZE - newW) / 2.0f;
        padH = (INPUT_SIZE - newH) / 2.0f;

        // Scale image preserving aspect ratio, then center it on a gray 640×640 canvas
        Image scaled = input.resize(newW, newH, true);
        java.awt.image.BufferedImage scaledBuf = (java.awt.image.BufferedImage) scaled.getWrappedImage();

        java.awt.image.BufferedImage canvas =
                new java.awt.image.BufferedImage(INPUT_SIZE, INPUT_SIZE, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = canvas.createGraphics();
        g.setColor(new java.awt.Color(114, 114, 114));
        g.fillRect(0, 0, INPUT_SIZE, INPUT_SIZE);
        g.drawImage(scaledBuf, (int) padW, (int) padH, null);
        g.dispose();

        // Convert HWC → CHW float array normalized to [0, 1]
        float[] chwArray = new float[3 * INPUT_SIZE * INPUT_SIZE];
        int channelSize = INPUT_SIZE * INPUT_SIZE;
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int rgb = canvas.getRGB(x, y);
                int idx = y * INPUT_SIZE + x;
                chwArray[idx] = ((rgb >> 16) & 0xFF) / 255.0f; // R
                chwArray[channelSize + idx] = ((rgb >> 8) & 0xFF) / 255.0f; // G
                chwArray[2 * channelSize + idx] = (rgb & 0xFF) / 255.0f; // B
            }
        }
        NDArray array = ctx.getNDManager().create(chwArray, new Shape(1, 3, INPUT_SIZE, INPUT_SIZE));
        return new NDList(array);
    }

    @Override
    public DetectedObjects processOutput(TranslatorContext ctx, NDList list) {
        float[] flatData = list.get(0).toFloatArray();
        int numAnchors = 8400; // YOLOv5 default for 640×640 input

        List<Rectangle> candidates = new ArrayList<>();
        List<Double> scores = new ArrayList<>();

        for (int i = 0; i < numAnchors; i++) {
            float conf = flatData[4 * numAnchors + i]; // already sigmoid-activated
            if (conf < THRESHOLD) continue;

            float cx = flatData[0 * numAnchors + i];
            float cy = flatData[1 * numAnchors + i];
            float w = flatData[2 * numAnchors + i];
            float h = flatData[3 * numAnchors + i];

            // Map from 640×640 letterbox space → original image space (normalized 0–1)
            double x1 = ((cx - w / 2.0) - padW) / scale / origW;
            double y1 = ((cy - h / 2.0) - padH) / scale / origH;
            double width = w / scale / origW;
            double height = h / scale / origH;

            candidates.add(new Rectangle(x1, y1, width, height));
            scores.add((double) conf);
        }

        return applyNMS(candidates, scores);
    }

    private DetectedObjects applyNMS(List<Rectangle> boxes, List<Double> scores) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < boxes.size(); i++) indices.add(i);
        indices.sort((a, b) -> Double.compare(scores.get(b), scores.get(a)));

        List<String> finalClasses = new ArrayList<>();
        List<Double> finalScores = new ArrayList<>();
        List<BoundingBox> finalBoxes = new ArrayList<>();
        boolean[] suppressed = new boolean[boxes.size()];

        for (int i = 0; i < indices.size(); i++) {
            int idx = indices.get(i);
            if (suppressed[idx]) continue;

            finalClasses.add(CLASSES.get(0));
            finalScores.add(scores.get(idx));
            finalBoxes.add(boxes.get(idx));

            for (int j = i + 1; j < indices.size(); j++) {
                int other = indices.get(j);
                if (!suppressed[other] && iou(boxes.get(idx), boxes.get(other)) > IOU_THRESHOLD) {
                    suppressed[other] = true;
                }
            }
        }
        return new DetectedObjects(finalClasses, finalScores, finalBoxes);
    }

    private double iou(Rectangle a, Rectangle b) {
        double x1 = Math.max(a.getX(), b.getX());
        double y1 = Math.max(a.getY(), b.getY());
        double x2 = Math.min(a.getX() + a.getWidth(), b.getX() + b.getWidth());
        double y2 = Math.min(a.getY() + a.getHeight(), b.getY() + b.getHeight());
        double intersection = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
        return intersection / (a.getWidth() * a.getHeight() + b.getWidth() * b.getHeight() - intersection);
    }
}
