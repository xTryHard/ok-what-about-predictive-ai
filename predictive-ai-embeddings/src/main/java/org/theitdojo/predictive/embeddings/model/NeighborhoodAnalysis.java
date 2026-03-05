package org.theitdojo.predictive.embeddings.model;

public record NeighborhoodAnalysis(
        int k,
        long churnYes,
        long churnNo,
        double averageSimilarity
) {
    public String riskSignal() {
        if (k <= 0) return "UNKNOWN";
        double churnRate = (double) churnYes / (double) k;
        if (churnRate >= 0.7) return "HIGH";
        if (churnRate >= 0.4) return "MEDIUM";
        return "LOW";
    }
}
