# predictive-ai-embeddings

Vector search and KNN-based customer similarity analysis — built from scratch without a vector database.

## What It Does

Converts telecom customers into dense feature vectors using Tribuo, then performs brute-force KNN search with cosine similarity to find similar customers and assess churn risk based on neighborhood composition.

## How It Works

### 1. Vectorization

`TribuoDenseVectorizer` converts Tribuo `Example` objects into dense `double[]` vectors using the trained feature map. Each vector is L2-normalized for cosine similarity.

### 2. Index Building

`CustomerVectorSearchService.buildIndex()` reads customers from CSV, processes them through Tribuo's data pipeline, vectorizes each one, and returns a `CustomerVectorIndex` — an in-memory index with O(1) lookup by customer ID.

### 3. KNN Search

`BruteForceKnn.topK()` scans all vectors, computes cosine similarity against the query, and returns the top-K nearest neighbors using a priority queue.

### 4. Neighborhood Analysis

`NeighborhoodAnalysis` examines the churn labels of a customer's K nearest neighbors and produces a risk signal:
- **HIGH** — majority of neighbors have churn=Yes
- **MEDIUM** — mixed neighborhood
- **LOW** — majority have churn=No

## Key Classes

| Class | Purpose |
|-------|---------|
| `CustomerVectorSearchService` | Facade: builds index, finds similar customers, analyzes neighborhoods |
| `BruteForceKnn` | Brute-force KNN with pluggable similarity metric |
| `CosineSimilarity` | Cosine similarity implementation |
| `TribuoDenseVectorizer` | Converts Tribuo examples to L2-normalized dense vectors |
| `CustomerVectorIndex` | In-memory index: customers + vectors + ID lookup |
| `NeighborhoodAnalysis` | Churn risk assessment from KNN results |
| `TelcoCsvCustomerReader` | CSV parser for telco customer data |

## Dependencies

- Tribuo Core 4.3.2 (feature maps and data pipeline)
- `predictive-ai-tribuo` (dataset factory)
- `predictive-ai-core`

## Demo Flow

From the main menu, choose **4) Embeddings / Vector Search (KNN)**, then:
- **Build Index** — Vectorize all customers from the telco dataset
- **Find Similar** — Enter a customer ID and see the K most similar customers
- **Neighborhood Analysis** — Assess churn risk based on neighbor composition
