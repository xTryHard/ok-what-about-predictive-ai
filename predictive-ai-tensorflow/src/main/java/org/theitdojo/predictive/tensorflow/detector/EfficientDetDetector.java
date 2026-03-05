package org.theitdojo.predictive.tensorflow.detector;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.SessionFunction;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.ByteDataBuffer;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TUint8;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads an EfficientDet SavedModel and runs object detection inference.
 */
public class EfficientDetDetector implements AutoCloseable {

    private final SavedModelBundle model;
    private final SessionFunction servingFn;

    public EfficientDetDetector(String modelDir) {
        this.model = SavedModelBundle.load(modelDir, "serve");
        this.servingFn = model.function("serving_default");
    }

    /**
     * Run detection on a BufferedImage. Returns detections above the given
     * confidence threshold.
     */
    public List<Detection> detect(BufferedImage image, float threshold) {
        int h = image.getHeight();
        int w = image.getWidth();

        // Build [1, H, W, 3] uint8 tensor
        byte[] pixels = new byte[h * w * 3];
        int idx = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = image.getRGB(x, y);
                pixels[idx++] = (byte) ((rgb >> 16) & 0xFF); // R
                pixels[idx++] = (byte) ((rgb >> 8) & 0xFF);  // G
                pixels[idx++] = (byte) (rgb & 0xFF);          // B
            }
        }

        ByteDataBuffer buf = DataBuffers.of(pixels);
        try (TUint8 inputTensor = TUint8.tensorOf(Shape.of(1, h, w, 3), buf)) {
            // Call via the signature function — handles name mapping automatically
            try (var results = servingFn.call(Map.of("input_tensor", inputTensor))) {

                try (TFloat32 boxes = (TFloat32) results.get("detection_boxes").get();
                     TFloat32 scores = (TFloat32) results.get("detection_scores").get();
                     TFloat32 classes = (TFloat32) results.get("detection_classes").get();
                     TFloat32 numDet = (TFloat32) results.get("num_detections").get()) {

                    int count = (int) numDet.getFloat(0);
                    List<Detection> detections = new ArrayList<>();

                    for (int i = 0; i < count; i++) {
                        float score = scores.getFloat(0, i);
                        if (score < threshold) continue;

                        int classId = (int) classes.getFloat(0, i);
                        float ymin = boxes.getFloat(0, i, 0);
                        float xmin = boxes.getFloat(0, i, 1);
                        float ymax = boxes.getFloat(0, i, 2);
                        float xmax = boxes.getFloat(0, i, 3);

                        detections.add(new Detection(classId, score, ymin, xmin, ymax, xmax));
                    }

                    return detections;
                }
            }
        }
    }

    public List<Detection> detect(BufferedImage image) {
        return detect(image, 0.5f);
    }

    @Override
    public void close() {
        model.close();
    }

    /**
     * A single detection result.
     */
    public record Detection(int classId, float score, float ymin, float xmin, float ymax, float xmax) {

        public String className() {
            return COCO_LABELS.getOrDefault(classId, "class_" + classId);
        }
    }

    // COCO 90-class label map (1-indexed, matching TF detection API conventions)
    private static final Map<Integer, String> COCO_LABELS = Map.ofEntries(
            Map.entry(1, "person"), Map.entry(2, "bicycle"), Map.entry(3, "car"),
            Map.entry(4, "motorcycle"), Map.entry(5, "airplane"), Map.entry(6, "bus"),
            Map.entry(7, "train"), Map.entry(8, "truck"), Map.entry(9, "boat"),
            Map.entry(10, "traffic light"), Map.entry(11, "fire hydrant"),
            Map.entry(13, "stop sign"), Map.entry(14, "parking meter"),
            Map.entry(15, "bench"), Map.entry(16, "bird"), Map.entry(17, "cat"),
            Map.entry(18, "dog"), Map.entry(19, "horse"), Map.entry(20, "sheep"),
            Map.entry(21, "cow"), Map.entry(22, "elephant"), Map.entry(23, "bear"),
            Map.entry(24, "zebra"), Map.entry(25, "giraffe"), Map.entry(27, "backpack"),
            Map.entry(28, "umbrella"), Map.entry(31, "handbag"), Map.entry(32, "tie"),
            Map.entry(33, "suitcase"), Map.entry(34, "frisbee"), Map.entry(35, "skis"),
            Map.entry(36, "snowboard"), Map.entry(37, "sports ball"),
            Map.entry(38, "kite"), Map.entry(39, "baseball bat"),
            Map.entry(40, "baseball glove"), Map.entry(41, "skateboard"),
            Map.entry(42, "surfboard"), Map.entry(43, "tennis racket"),
            Map.entry(44, "bottle"), Map.entry(46, "wine glass"), Map.entry(47, "cup"),
            Map.entry(48, "fork"), Map.entry(49, "knife"), Map.entry(50, "spoon"),
            Map.entry(51, "bowl"), Map.entry(52, "banana"), Map.entry(53, "apple"),
            Map.entry(54, "sandwich"), Map.entry(55, "orange"), Map.entry(56, "broccoli"),
            Map.entry(57, "carrot"), Map.entry(58, "hot dog"), Map.entry(59, "pizza"),
            Map.entry(60, "donut"), Map.entry(61, "cake"), Map.entry(62, "chair"),
            Map.entry(63, "couch"), Map.entry(64, "potted plant"), Map.entry(65, "bed"),
            Map.entry(67, "dining table"), Map.entry(70, "toilet"), Map.entry(72, "tv"),
            Map.entry(73, "laptop"), Map.entry(74, "mouse"), Map.entry(75, "remote"),
            Map.entry(76, "keyboard"), Map.entry(77, "cell phone"),
            Map.entry(78, "microwave"), Map.entry(79, "oven"),
            Map.entry(80, "toaster"), Map.entry(81, "sink"),
            Map.entry(82, "refrigerator"), Map.entry(84, "book"),
            Map.entry(85, "clock"), Map.entry(86, "vase"), Map.entry(87, "scissors"),
            Map.entry(88, "teddy bear"), Map.entry(89, "hair drier"),
            Map.entry(90, "toothbrush")
    );
}
