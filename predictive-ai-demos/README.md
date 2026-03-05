# predictive-ai-demos

Interactive CLI orchestrator for the DevNexus talk **"OK, But What About Predictive AI?"**. This module wires all other modules into a single menu-driven application designed for live stage demonstrations.

## Running the Demos

```bash
# Build everything first
mvn -q install -DskipTests

# Launch the interactive menu
mvn -q exec:java -pl predictive-ai-demos \
  -Dexec.mainClass=org.theitdojo.predictive.demos.MainMenu
```

## Demo Guide

### 1) Tribuo Training + Evaluation

**Module:** `predictive-ai-tribuo` | **Class:** `TribuoDemoMenu`

Demonstrates classical ML on the JVM. Three sub-options:

| Option | What It Does |
|--------|-------------|
| EDA | Prints label distribution and dataset statistics from the telco churn CSV |
| Model Benchmark | Trains Logistic Regression, Linear SVM, and Random Forest; prints accuracy, precision, recall, F1 |
| Provenance Export | Serializes full model provenance (data source, trainer config, features) to JSON files |

**Talking points:** Pure Java ML training, Tribuo's provenance tracking, class weighting for recall.

### 2) Inference (Local ONNX / Python API)

**Module:** `predictive-ai-inference` | **Class:** `InferenceDemoMenu`

Shows the Port & Adapter pattern with two `Predictor` implementations:

| Option | What It Does |
|--------|-------------|
| Local ONNX Runtime | Runs churn predictions using `OnnxRuntimePredictor` against 10 fixture customers |
| Python API Service | Same predictions via `HttpPredictor` (requires Python sidecar on port 9999) |
| Benchmark | Generates N random customers, benchmarks local vs. remote latency (avg, min, max, p95) |

**Talking points:** Sub-millisecond local inference, latency comparison, same interface for both backends.

**Note:** For the Python API option, start the sidecar first:
```bash
cd python-service && source venv/bin/activate && python app.py
```

### 3) Bat Tracking (YOLOv5 ONNX via DJL)

**Module:** `predictive-ai-djl` | **Class:** `BatTrackingRunner`

Real-time bat detection in images or video.

| Prompt | Input |
|--------|-------|
| Mode | `1` for image, `2` for video |
| Input | Local file path or URL |
| Output | Where to save the annotated result |

**Talking points:** Letterbox preprocessing, ONNX inference on JVM, NMS, video frame-by-frame processing.

### 4) Embeddings / Vector Search (KNN)

**Module:** `predictive-ai-embeddings` | **Class:** `EmbeddingsDemoMenu`

Builds an in-memory vector index from the telco dataset and finds similar customers using cosine similarity.

| Option | What It Does |
|--------|-------------|
| High-risk fixture | Shows 5 nearest neighbors for a known high-churn customer |
| Low-risk fixture | Shows 5 nearest neighbors for a known low-churn customer |
| Custom search | Enter any customer ID and find similar customers |

Output includes similarity scores, neighbor profiles, churn mix, and a risk signal (HIGH/MEDIUM/LOW).

**Talking points:** No vector database needed, brute-force KNN, churn risk from neighborhood composition.

### 5) EfficientDet (TensorFlow Java)

**Module:** `predictive-ai-tensorflow` | **Class:** `EfficientDetRunner`

COCO 90-class object detection using a TensorFlow SavedModel.

| Option | What It Does |
|--------|-------------|
| Detect | Runs EfficientDet on an image, draws bounding boxes, saves result |
| Benchmark | Measures inference latency over multiple runs |

**Talking points:** TensorFlow Java SavedModel loading, uint8 tensor input, 90 COCO classes.

### 6) Baseball Tracking (YOLOv11 ONNX via DJL)

**Module:** `predictive-ai-djl` | **Class:** `BaseballTrackingRunner`

Multi-class detection (Baseball, Homeplate, Rubber) using a YOLOv11 model. Same image/video flow as bat tracking.

**Talking points:** Multi-class YOLOv11, different model architecture on the same DJL pipeline.

### 7) Combined Tracking (Bat + Baseball)

**Module:** `predictive-ai-djl` | **Class:** `CombinedTrackingRunner`

Runs both detectors simultaneously on the same input. Bat detections draw in **green**, baseball detections in **cyan**.

**Talking points:** Multiple models on the same frame, color-coded overlay, real-time combined detection.

### 8) RL: Adaptive Offer Optimization (Bandit)

**Module:** `predictive-ai-rl-bandit` | **Class:** `RLBanditDemoMenu`

Contextual multi-armed bandit simulation. Configurable number of steps (default: 500).

Output shows periodic snapshots as the bandit learns:
- Bandit vs. random average reward
- Acceptance rates
- Offer distribution bar charts showing strategy shifts
- Final lift percentage over random baseline

**Talking points:** Exploration vs. exploitation, online learning, Tribuo regression as reward predictor.

### 9) Clustering: Customer Segments (K-Means + PCA)

**Module:** `predictive-ai-clustering` | **Class:** `ClusteringDemoMenu`

K-Means segmentation with PCA visualization. Configurable parameters:
- Number of clusters (default: 4)
- Maximum points to plot (default: 1200)
- Output image path (default: `customer_clusters.png`)

Outputs a PNG scatter plot with color-coded clusters and per-cluster size summaries.

**Talking points:** Tribuo K-Means, PCA from scratch with Commons Math3, unsupervised learning.

## Architecture

`MainMenu` is the single entry point. It creates one `Scanner` and passes it to all sub-menus and runners. Each demo module is self-contained — the demos module only handles menu routing and user interaction.

```
MainMenu
  ├── TribuoDemoMenu        → predictive-ai-tribuo
  ├── InferenceDemoMenu      → predictive-ai-inference
  ├── BatTrackingRunner      → predictive-ai-djl
  ├── EmbeddingsDemoMenu     → predictive-ai-embeddings
  ├── EfficientDetRunner     → predictive-ai-tensorflow
  ├── BaseballTrackingRunner → predictive-ai-djl
  ├── CombinedTrackingRunner → predictive-ai-djl
  ├── RLBanditDemoMenu       → predictive-ai-rl-bandit
  └── ClusteringDemoMenu     → predictive-ai-clustering
```

## Dependencies

This module depends on all other modules in the project. It contains no ML logic — only CLI orchestration.
