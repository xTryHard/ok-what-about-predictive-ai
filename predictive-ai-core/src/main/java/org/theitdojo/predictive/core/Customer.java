package org.theitdojo.predictive.core;

/**
 * Minimal customer record used by both training + inference demos.
 *
 * Note: "actual" is optional for inference tests.
 */
public record Customer(
        String id,
        String gender,
        float seniorCitizen,
        String partner,
        String dependents,
        float tenure,
        String phoneService,
        String multipleLines,
        String internetService,
        String onlineSecurity,
        String onlineBackup,
        String deviceProtection,
        String techSupport,
        String streamingTV,
        String streamingMovies,
        String contract,
        String paperlessBilling,
        String paymentMethod,
        float monthlyCharges,
        float totalCharges,
        String actual
) {}
