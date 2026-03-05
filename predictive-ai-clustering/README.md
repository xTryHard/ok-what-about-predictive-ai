# predictive-ai-clustering

K-Means customer segmentation with PCA dimensionality reduction and 2D scatter plot visualization.

## What It Does

Groups telecom customers into segments based on behavioral features, then projects the high-dimensional clusters down to 2D using PCA and renders a scatter plot.

## How It Works

### 1. Data Loading

`TelcoCustomerLoader` reads the telco churn CSV from the classpath and converts rows to `Customer` records.

### 2. Feature Vectorization

`CustomerVectorizer` extracts selected features from each customer into a `double[]` vector. `ClusteringFeatures` defines which features are used for clustering.

### 3. K-Means Clustering

`KMeansClusteringService` runs Tribuo's `KMeansTrainer` with L2 distance. It supports reservoir sampling for large datasets (configurable `maxPoints` limit) and computes cluster centroids and assignments.

### 4. PCA Dimensionality Reduction

`Pca2D` implements PCA from scratch using Apache Commons Math3:
- Computes the covariance matrix of the input data
- Performs eigendecomposition to find principal components
- Projects data onto the top 2 eigenvectors

### 5. Visualization

`ClusterPlotRenderer` renders a 2D scatter plot as a PNG image, with each cluster drawn in a different color.

### Pipeline

`ClusteringPipeline` orchestrates the entire flow: load data, cluster, reduce dimensions, render plot.

## Key Classes

| Class | Purpose |
|-------|---------|
| `ClusteringPipeline` | End-to-end pipeline: CSV to cluster visualization |
| `KMeansClusteringService` | K-Means clustering via Tribuo with reservoir sampling |
| `Pca2D` | PCA implementation using covariance matrix eigendecomposition |
| `ClusterPlotRenderer` | Renders 2D scatter plot as PNG |
| `CustomerVectorizer` | Converts Customer to feature vector |
| `ClusteringFeatures` | Defines which features are used for clustering |
| `TelcoCustomerLoader` | Loads customers from CSV |
| `ClusterPoint2D` | Record: `x`, `y`, `clusterId` for 2D visualization |

## Dependencies

- Tribuo 4.3.2 (`tribuo-all` — KMeansTrainer)
- Apache Commons Math3 3.6.1 (eigendecomposition for PCA)
- `predictive-ai-core`

## Demo Flow

From the main menu, choose **9) Clustering: Customer Segments (K-Means + PCA)** to:
- Configure the number of clusters (K) and maximum points
- Run K-Means segmentation on the telco dataset
- Generate a 2D PCA scatter plot saved as a PNG file
