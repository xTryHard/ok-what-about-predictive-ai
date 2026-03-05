# predictive-ai-core

Shared domain model, interfaces, and utilities used by all other modules.

## What It Contains

### Domain Records

- **`Customer`** — Record with 20 attributes representing a telecom customer (demographics, services, billing). Used as the input for churn prediction, embeddings, clustering, and bandit simulation.
- **`Prediction`** — Record with `label`, `confidence`, and `latencyMs`. Includes a factory method `unknown(latencyMs)` for cases where the model returns an unrecognized label.

### Feature Enum

`Feature` defines all 14 model features used for churn prediction:

| Type | Features |
|------|----------|
| Numeric (4) | `SENIOR_CITIZEN`, `TENURE`, `MONTHLY_CHARGES`, `TOTAL_CHARGES` |
| Categorical (10) | `GENDER`, `PARTNER`, `DEPENDENTS`, `PHONE_SERVICE`, `MULTIPLE_LINES`, `INTERNET_SERVICE`, `ONLINE_SECURITY`, `ONLINE_BACKUP`, `DEVICE_PROTECTION`, `TECH_SUPPORT`, `STREAMING_TV`, `STREAMING_MOVIES`, `CONTRACT`, `PAPERLESS_BILLING`, `PAYMENT_METHOD` |

This enum is the single source of truth — CSV column names, ONNX tensor names, and HTTP API payload keys all derive from it.

### Interfaces

- **`Predictor`** — Port interface: `Prediction predict(Customer customer) throws Exception`. Implemented by `OnnxRuntimePredictor` (local) and `HttpPredictor` (remote) in the inference module.

### Utilities

- **`ResourceFiles`** — Copies classpath resources to temp files at runtime: `Path copyToTempFile(String classpathResource, String suffix)`. Used by all modules that load models from JAR resources.
- **`InputResolver`** — Resolves user input (local file path or URL) to a local `Path`. Downloads URLs to temp files. Returns a `Resolved` record with `path()` and `temporary()` flag. Used by DJL and TensorFlow modules for image/video input handling.

## Dependencies

- Jackson Databind (for JSON serialization support)

## Usage

This module has no runnable entry point. It is a dependency of every other module in the project.
