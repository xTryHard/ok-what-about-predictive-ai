# predictive-ai-tensorflow

Object detection using [TensorFlow Java](https://www.tensorflow.org/jvm) with a pre-trained EfficientDet SavedModel (COCO 90-class).

## What It Does

Loads an EfficientDet SavedModel, runs inference on images, and draws bounding boxes with class labels and confidence scores. Supports the full COCO object detection class set (90 classes: person, car, dog, sports ball, etc.).

## How It Works

### 1. Model Loading

`EfficientDetDetector` loads the SavedModel from the classpath using TensorFlow Java's `SavedModelBundle`. It resolves the `serving_default` signature function for inference.

### 2. Image Preprocessing

Input images are read as `BufferedImage`, converted to a `uint8` tensor with shape `[1, height, width, 3]` (batch, height, width, RGB channels). No resizing or normalization is needed — EfficientDet handles this internally.

### 3. Inference

The model returns four tensors:
- Detection boxes (normalized `[ymin, xmin, ymax, xmax]`)
- Detection classes (integer COCO class IDs)
- Detection scores (confidence values)
- Number of detections

### 4. Filtering and Rendering

`EfficientDetDetector.detect()` filters results by a configurable confidence threshold and returns a list of `Detection` records. `DetectionRenderer` draws color-coded bounding boxes with class names on the original image.

## Key Classes

| Class | Purpose |
|-------|---------|
| `EfficientDetDetector` | Loads SavedModel, runs inference, returns `Detection` records |
| `EfficientDetRunner` | CLI entry: detect objects or run benchmarks |
| `EfficientDetDemo` | Standalone demo runner |
| `DetectionRenderer` | Draws color-coded bounding boxes with COCO class names |
| `BenchmarkRunner` | Benchmarks EfficientDet inference latency |

## Model

The EfficientDet SavedModel is stored in `src/main/resources/models/efficientdet/`:
- `saved_model.pb` — Graph definition
- `variables/` — Model weights

This is a standard TensorFlow SavedModel directory structure.

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| TensorFlow Java | 1.1.0 | SavedModel loading and inference |
| SLF4J Simple | 2.0.17 | Logging |
| `predictive-ai-core` | | InputResolver, ResourceFiles |

## Demo Flow

From the main menu, choose **5) EfficientDet (TensorFlow Java)**, then:
- **Detect** — Run object detection on an image (prompts for input path/URL and output path)
- **Benchmark** — Measure inference latency over multiple runs
