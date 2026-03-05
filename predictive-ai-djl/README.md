# predictive-ai-djl

Real-time object detection for bats and baseballs in images and video using ONNX models running on the JVM via [DJL](https://djl.ai/) and ONNX Runtime.

## What It Does

- Detects **bats** using a YOLOv5 ONNX model
- Detects **baseballs** (and home plate, rubber) using a YOLOv11 ONNX model
- Processes **single images** or **video frame-by-frame** with annotated bounding boxes
- Supports **combined tracking** — both detectors running on the same video with color-coded results
- Benchmarks local DJL inference and remote Python service inference

## Prerequisites

[Git LFS](https://git-lfs.com/) is required — the ONNX model files are stored in Git LFS:

```bash
git lfs install   # once per machine
git lfs pull      # downloads the actual model files
```

Without this step, model files will be small pointer stubs and inference will fail.

## Object Detection Pipeline

### 1. Letterbox Preprocessing

Input images are resized to fit within a 640x640 canvas while preserving aspect ratio. Remaining area is padded with gray (value 114). The scale factor and padding offsets are saved for coordinate mapping in step 4.

### 2. Inference

The preprocessed image is converted to a CHW (channel-height-width) float array normalized to `[0, 1]` and fed into the ONNX model via DJL's `Predictor`. The model produces 8400 anchor predictions per frame.

### 3. Confidence Filtering + NMS

Predictions below the confidence threshold are discarded. Surviving boxes go through **Non-Maximum Suppression (NMS)**:
1. Sort by confidence (highest first)
2. Keep top box, suppress any remaining box whose IoU exceeds the threshold
3. Repeat for the next surviving box

### 4. Coordinate Mapping

Model output coordinates (in 640x640 letterbox space) are mapped back to original image dimensions by removing padding and undoing the scale factor.

## Key Classes

| Class | Purpose |
|-------|---------|
| `ObjectDetector` | Model loader with factory methods `forBats()` and `forBaseball()` |
| `BatDetectorTranslator` | YOLOv5 pre/post-processing (single class: bat) |
| `BaseballDetectorTranslator` | YOLOv11 pre/post-processing (multi-class: Homeplate, Baseball, Rubber) |
| `ImageProcessor` | Detects objects in a single image, saves annotated result |
| `VideoProcessor` | Frame-by-frame detection with FFmpeg (every 2nd frame inferred, others reuse last result) |
| `CombinedVideoProcessor` | Runs two detectors on the same video with separate colors |
| `DetectionRenderer` | Draws bounding boxes and confidence labels on images |
| `BatTrackingRunner` | CLI entry for bat detection |
| `BaseballTrackingRunner` | CLI entry for baseball detection |
| `CombinedTrackingRunner` | CLI entry for combined bat + baseball detection |
| `BenchmarkRunner` | Benchmarks local DJL inference latency |
| `RemoteBenchmarkRunner` | Benchmarks remote Python service inference |
| `TrackingPrompts` | Shared CLI prompt logic for mode, input, and output selection |

## Tuning Parameters

| Parameter | Location | Default | Effect |
|-----------|----------|---------|--------|
| Confidence threshold | `BatDetectorTranslator` | 0.5 | Lower = more detections, higher = fewer but more confident |
| IoU (NMS) threshold | `BatDetectorTranslator` | 0.3 | Lower = more aggressive duplicate suppression |
| Confidence threshold | `BaseballDetectorTranslator` | 0.5 | Same as above, for baseball model |
| Frame skip | `VideoProcessor` | 2 | Inference runs every Nth frame; others reuse last result |

## Models

| Model | File | Architecture | Classes |
|-------|------|-------------|---------|
| Bat detection | `bat_tracking.onnx` | YOLOv5 | bat |
| Baseball detection | `ball_tracking_v4-YOLOv11.onnx` | YOLOv11 | Homeplate, Baseball, Rubber, background |

Models are stored in `src/main/resources/models/` and copied to `target/models/` at build time.

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| DJL API | 0.36.0 | Model loading and inference abstraction |
| DJL ONNX Runtime Engine | 0.36.0 | Executes ONNX models on CPU |
| JavaCV | 1.5.10 | Video frame extraction and encoding (FFmpeg) |
| WebP ImageIO | 0.1.6 | WebP image format support |
| Jackson Databind | | JSON for remote benchmark payloads |
| SLF4J Simple | 2.0.17 | Logging |
| `predictive-ai-core` | | InputResolver, ResourceFiles |

## Demo Flow

From the main menu:
- **3) Bat Tracking** — Detect bats in an image or video (YOLOv5)
- **6) Baseball Tracking** — Detect baseballs in an image or video (YOLOv11)
- **7) Combined Tracking** — Both detectors on the same input (green = bat, cyan = baseball)

Each runner prompts for mode (image/video), input path or URL, and output path.
