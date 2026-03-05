package org.theitdojo.predictive.embeddings.search;

import org.theitdojo.predictive.embeddings.model.SimilarityResult;
import org.theitdojo.predictive.embeddings.similarity.SimilarityMetric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Simple brute-force KNN over in-memory vectors.
 * For the talk: this is "vector search without a vector database".
 */
public final class BruteForceKnn<T> {

    private final SimilarityMetric metric;

    public BruteForceKnn(SimilarityMetric metric) {
        this.metric = metric;
    }

    public List<SimilarityResult<T>> topK(double[] query, List<T> items, List<double[]> vectors, int k, int excludeIndex) {
        if (k <= 0) return List.of();
        int n = Math.min(items.size(), vectors.size());
        PriorityQueue<SimilarityResult<T>> heap = new PriorityQueue<>(Comparator.comparingDouble(SimilarityResult::similarity));

        for (int i = 0; i < n; i++) {
            if (i == excludeIndex) continue;
            double sim = metric.similarity(query, vectors.get(i));
            var res = new SimilarityResult<>(items.get(i), sim);

            if (heap.size() < k) {
                heap.add(res);
            } else if (sim > heap.peek().similarity()) {
                heap.poll();
                heap.add(res);
            }
        }

        ArrayList<SimilarityResult<T>> out = new ArrayList<>(heap);
        out.sort(Comparator.comparingDouble(SimilarityResult<T>::similarity).reversed());
        return out;
    }
}
