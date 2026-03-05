# predictive-ai-inference

Inference adapters implementing the `Predictor` interface from `core` — one for local ONNX Runtime execution, one for a remote Python Flask sidecar.

## Architecture

This module demonstrates the **Port & Adapter** pattern:

```
Predictor (interface, in core)
    |
    ├── OnnxRuntimePredictor   (local, sub-millisecond)
    └── HttpPredictor          (remote, HTTP POST to Python sidecar)
```

Both accept a `Customer` record and return a `Prediction` with label, confidence, and latency.

## Key Classes

| Class | Purpose |
|-------|---------|
| `OnnxRuntimePredictor` | Loads `churn_model.onnx` from classpath, builds input tensors from `Feature` enum, runs inference locally |
| `HttpPredictor` | Sends customer features as JSON to `http://localhost:5001/predict`, parses response |
| `BenchmarkRunner` | Runs N predictions and reports latency statistics (avg, min, max, p95) |
| `BenchmarkResult` | Record: `avg`, `min`, `max`, `p95` latencies with factory `from(List<Long>)` |
| `CustomerFixtures` | 10 hardcoded customers for repeatable smoke testing |
| `RandomCustomerGenerator` | Generates random customers with seeded `Random` for benchmarking |

## Model

The module includes `churn_model.onnx` in its resources — a binary classification model that predicts customer churn (Yes/No) with confidence scores.

## Dependencies

- ONNX Runtime 1.18.0
- Jackson Databind (JSON payloads for HTTP predictor)
- `predictive-ai-core`

## Demo Flow

From the main menu, choose **2) Inference (Local ONNX / Python API)**, then:
- **Local ONNX** — Run predictions using `OnnxRuntimePredictor` against smoke test customers
- **Python API** — Run predictions via `HttpPredictor` (requires the Python sidecar running)
- **Benchmark** — Compare latency of local vs. remote inference
