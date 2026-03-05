package org.theitdojo.predictive.clustering;

/**
 * Feature set for the clustering demo.
 *
 * We intentionally keep this small + numeric so:
 * - K-Means distance makes sense
 * - PCA visualization is stable and interpretable
 */
public enum ClusteringFeatures {
    TENURE,
    MONTHLY_CHARGES,
    TOTAL_CHARGES,
    SENIOR_CITIZEN,
    CONTRACT_LENGTH,
    SERVICE_COUNT,
    PAPERLESS_BILLING,
    FIBER_OPTIC
}
