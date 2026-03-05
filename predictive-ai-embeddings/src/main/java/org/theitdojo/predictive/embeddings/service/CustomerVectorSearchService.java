package org.theitdojo.predictive.embeddings.service;

import org.theitdojo.predictive.core.Customer;
import org.theitdojo.predictive.core.ResourceFiles;
import org.theitdojo.predictive.embeddings.io.TelcoCsvCustomerReader;
import org.theitdojo.predictive.embeddings.model.CustomerVectorIndex;
import org.theitdojo.predictive.embeddings.model.NeighborhoodAnalysis;
import org.theitdojo.predictive.embeddings.model.SimilarityResult;
import org.theitdojo.predictive.embeddings.search.BruteForceKnn;
import org.theitdojo.predictive.embeddings.similarity.CosineSimilarity;
import org.theitdojo.predictive.embeddings.vectorization.ExampleVectorizer;
import org.theitdojo.predictive.embeddings.vectorization.TribuoDenseVectorizer;
import org.theitdojo.predictive.tribuo.ChurnDatasetFactory;
import org.tribuo.ImmutableFeatureMap;
import org.tribuo.MutableDataset;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade used by demos:
 * - Builds an in-memory vector index from the Telco churn CSV (same one used by Tribuo demos).
 * - Runs KNN to find similar customers.
 *
 * "Vector search" here is just KNN over embeddings.
 */
public final class CustomerVectorSearchService {

    private final BruteForceKnn<Customer> knn;
    private final ExampleVectorizer vectorizer;

    public CustomerVectorSearchService() {
        this.knn = new BruteForceKnn<>(new CosineSimilarity());
        this.vectorizer = new TribuoDenseVectorizer();
    }

    public CustomerVectorIndex buildIndex(String csvClasspath) throws Exception {
        // 1) Build Tribuo dataset (for feature extraction / one-hot expansion)
        var factory = new ChurnDatasetFactory(csvClasspath, 0.7, 42L);
        var source = factory.buildDataSource();
        var dataset = new MutableDataset<>(source);
        var featureMap = new ImmutableFeatureMap(dataset.getFeatureMap());

        // 2) Read CSV into Customer records in the same row order
        Path csvPath = ResourceFiles.copyToTempFile(csvClasspath, ".csv");
        var customers = new TelcoCsvCustomerReader().read(csvPath);

        int n = Math.min(customers.size(), dataset.size());
        customers = customers.subList(0, n);

        // 3) Vectorize each example into a dense normalized vector
        var vectors = new java.util.ArrayList<double[]>(n);
        int i = 0;
        for (var ex : dataset) {
            if (i >= n) break;
            vectors.add(vectorizer.vectorize(ex, featureMap));
            i++;
        }

        // 4) Build id->index map
        Map<String, Integer> idToIndex = new HashMap<>();
        for (int idx = 0; idx < customers.size(); idx++) {
            idToIndex.put(customers.get(idx).id(), idx);
        }

        return new CustomerVectorIndex(customers, vectors, idToIndex);
    }

    public List<SimilarityResult<Customer>> findSimilar(CustomerVectorIndex index, String customerId, int k) {
        Integer idx = index.idToIndex().get(customerId);
        if (idx == null) {
            throw new IllegalArgumentException("Customer id not found in dataset: " + customerId);
        }

        double[] query = index.vectors().get(idx);
        return knn.topK(query, index.customers(), index.vectors(), k, idx);
    }

    public NeighborhoodAnalysis analyzeNeighborhood(CustomerVectorIndex index, String customerId, int k) {
        var top = findSimilar(index, customerId, k);

        long yes = top.stream().filter(r -> "Yes".equalsIgnoreCase(r.item().actual())).count();
        long no = top.size() - yes;
        double avg = top.stream().mapToDouble(SimilarityResult::similarity).average().orElse(0.0);

        return new NeighborhoodAnalysis(top.size(), yes, no, avg);
    }

    public static String shortProfile(Customer c) {
        return String.format(
                "%s | tenure=%.0f | $%.2f/mo | contract=%s | internet=%s | churn=%s",
                c.id(), c.tenure(), c.monthlyCharges(), c.contract(), c.internetService(), c.actual()
        );
    }
}
