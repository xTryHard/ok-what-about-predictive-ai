package org.theitdojo.predictive.core;

public record Prediction(String label, float confidence, long latencyMs) {
    public static Prediction unknown(long latencyMs) {
        return new Prediction("UNKNOWN", 0.0f, latencyMs);
    }
}
