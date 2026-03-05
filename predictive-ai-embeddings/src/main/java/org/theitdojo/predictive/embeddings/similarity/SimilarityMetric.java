package org.theitdojo.predictive.embeddings.similarity;

/**
 * Similarity metric for dense vectors.
 * Higher means "more similar".
 */
public interface SimilarityMetric {
    double similarity(double[] a, double[] b);
}
