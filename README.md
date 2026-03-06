# OK, But What About Predictive AI?

Companion code for the DevNexus talk **"OK, But What About Predictive AI?"** — a hands-on tour of predictive AI techniques running entirely on the JVM.

While the industry focuses on generative AI, predictive AI quietly drives the decisions that matter: churn prediction, real-time object detection, customer segmentation, adaptive optimization, and similarity search. This project demonstrates all of them using Java 25 and open-source ML libraries — no Python required for inference.

## What You'll Learn

- **Train and evaluate** classification models with Tribuo (Logistic Regression, SVM, Random Forest)
- **Run ONNX models locally** on the JVM with ONNX Runtime — and compare against a Python sidecar
- **Detect objects in images and video** using YOLOv5 and YOLOv11 via DJL + ONNX Runtime
- **Run EfficientDet inference** with TensorFlow Java SavedModel
- **Build a vector search engine** from scratch with brute-force KNN and cosine similarity
- **Optimize offers adaptively** with a contextual multi-armed bandit (epsilon-greedy + Tribuo regression)
- **Segment customers** using K-Means clustering with PCA dimensionality reduction

## Project Structure

```
what-about-predictive-ai/
├── predictive-ai-core/          # Domain model, interfaces, shared utilities
├── predictive-ai-tribuo/        # ML training & evaluation with Tribuo
├── predictive-ai-inference/     # ONNX Runtime + HTTP inference adapters
├── predictive-ai-djl/           # Object detection (DJL + ONNX, bat & baseball tracking)
├── predictive-ai-tensorflow/    # EfficientDet via TensorFlow Java
├── predictive-ai-embeddings/    # Vector search / KNN similarity
├── predictive-ai-rl-bandit/     # Contextual bandit (adaptive offer optimization)
├── predictive-ai-clustering/    # K-Means segmentation + PCA visualization
├── predictive-ai-demos/         # Interactive CLI menu wiring all modules
├── python-service/              # Optional Flask sidecar for HTTP inference
└── slides/                      # Presentation slides (PDF)
```

### Module Dependencies

```
core  <──  tribuo
  |          |
  |<── inference
  |
  |<── djl
  |
  |<── tensorflow
  |
  |<── embeddings  ──>  tribuo
  |
  |<── rl-bandit
  |
  |<── clustering
  |
  └<── demos  ──>  (all modules)
```

## Slides

The presentation slides are available in the [`slides/`](slides/) directory.

## Prerequisites

- **Java 25** (required — uses modern language features)
- **Maven 3.6+**
- **Git LFS** — ONNX model files are stored in Git LFS

## Quick Start

### 1. Clone and pull model files

```bash
git clone <repo-url>
cd what-about-predictive-ai
git lfs install   # once per machine
git lfs pull      # downloads ONNX and SavedModel files
```

### 2. Build all modules

```bash
mvn -q install -DskipTests
```

### 3. Run the interactive demo menu

```bash
mvn -q exec:java -pl predictive-ai-demos \
  -Dexec.mainClass=org.theitdojo.predictive.demos.MainMenu
```

You'll see:

```
====================================
 PREDICTIVE AI DEVNEXUS DEMOS
====================================
1) Tribuo Training + Evaluation
2) Inference (Local ONNX / Python API)
3) Bat Tracking (YOLOv5 ONNX via DJL)
4) Embeddings / Vector Search (KNN)
5) EfficientDet (TensorFlow Java)
6) Baseball Tracking (YOLOv11 ONNX via DJL)
7) Combined Tracking (Bat + Baseball)
8) RL: Adaptive Offer Optimization (Bandit)
9) Clustering: Customer Segments (K-Means + PCA)
10) Exit
```

### 4. (Optional) Start the Python sidecar

For comparing local ONNX inference against HTTP inference:

```bash
cd python-service
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python app.py
```

Then choose **Inference > Python API Service** in the main menu.

## Technologies

| Library | Version | Purpose |
|---------|---------|---------|
| Tribuo | 4.3.2 | ML training, evaluation, K-Means clustering, regression |
| ONNX Runtime | 1.18.0 | Local churn model inference |
| DJL + ONNX Runtime Engine | 0.36.0 | Object detection model loading and inference |
| TensorFlow Java | 1.1.0 | EfficientDet SavedModel inference |
| JavaCV | 1.5.10 | Video frame extraction and encoding (FFmpeg) |
| Jackson | 2.15.2 | JSON serialization (provenance export, HTTP payloads) |
| Apache Commons Math3 | 3.6.1 | PCA eigendecomposition for clustering visualization |
| SLF4J | 2.0.17 | Logging |

## Key Architectural Patterns

**Port & Adapter (Inference):** The `Predictor` interface in `core` is implemented by `OnnxRuntimePredictor` (local, sub-millisecond) and `HttpPredictor` (remote, Python Flask sidecar). Both accept a `Customer` record and return a `Prediction`.

**Feature Enum as Source of Truth:** The `Feature` enum defines all 14 model features with their types (numeric vs. categorical). CSV columns, ONNX tensor names, and HTTP payload keys all derive from this single definition.

**Object Detection Pipeline:** Input images go through letterbox preprocessing (aspect-ratio preserving resize to 640x640), ONNX inference via DJL, confidence filtering, and Non-Maximum Suppression (NMS) before coordinate mapping back to original image space.

**Model Resource Management:** ONNX models and SavedModel directories are stored as classpath resources, copied to `target/models/` at build time via `maven-resources-plugin`, and extracted to temp files at runtime via `ResourceFiles.copyToTempFile()`.

## Notes

- No unit tests — this is a demo project. Verification is done by running the interactive menu.
- Output files (`*-output.mp4`, `*-output.png`) are generated locally and gitignored.
- If you have a different ONNX Runtime version locally, change `onnxruntime.version` in the root `pom.xml`.
- Positive class weighting (churn=Yes weighted 4x) is intentional in `ChurnDatasetFactory` for recall optimization.
