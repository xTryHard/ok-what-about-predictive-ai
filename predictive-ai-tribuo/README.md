# predictive-ai-tribuo

ML training, evaluation, and model provenance using [Tribuo](https://tribuo.org/) — Oracle's Java ML library.

## What It Does

Trains and benchmarks three classification algorithms on a telecom churn dataset, then exports full model provenance as JSON.

### Training Pipeline

1. **`ChurnDatasetFactory`** loads `telco-customer-churn.csv` from the classpath, builds Tribuo `DataSource` objects, and splits into train/test sets. The `Feature` enum from `core` drives column processing. The positive class (churn=Yes) is weighted 4x for recall optimization.

2. **`TrainerRegistry`** provides three pre-configured trainers:
   - Logistic Regression (`LogisticRegressionTrainer`)
   - Linear SVM (`LinearSGDTrainer` with hinge loss)
   - Random Forest (`RandomForestTrainer`, 100 trees)

3. **`BenchmarkService`** trains each model, evaluates on the test set, and reports accuracy, precision, recall, F1, and training time.

4. **`ProvenanceExporter`** serializes Tribuo's built-in provenance metadata (data source, trainer config, feature transformations) to JSON files — one per model.

### Exploratory Data Analysis

**`EdaService`** prints label distribution and dataset statistics, useful for understanding class imbalance before training.

## Key Classes

| Class | Purpose |
|-------|---------|
| `ChurnDatasetFactory` | Loads CSV, builds weighted train/test split |
| `TrainerRegistry` | Registry of classification trainers |
| `BenchmarkService` | Trains, evaluates, and reports metrics |
| `EdaService` | Exploratory data analysis (label distribution) |
| `ProvenanceExporter` | Exports model provenance as JSON |

## Dataset

The module includes `telco-customer-churn.csv` in its resources — a standard telecom churn dataset with 21 columns (customer demographics, services subscribed, billing info, and churn label).

## Dependencies

- Tribuo 4.3.2 (`tribuo-all`)
- Jackson Databind + JSR310 (provenance JSON export)
- `predictive-ai-core`

## Demo Flow

From the main menu, choose **1) Tribuo Training + Evaluation**, then:
- **EDA** — View dataset label distribution
- **Benchmark** — Train all three models and compare metrics
- **Provenance** — Export model provenance to `provenance-exports/`
