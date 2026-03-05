package org.theitdojo.predictive.clustering;

import org.theitdojo.predictive.core.Customer;

import java.util.Locale;

/**
 * Converts a {@link Customer} into a compact numeric feature vector
 * used by the clustering demo.
 */
public final class CustomerVectorizer {

    private CustomerVectorizer() {}

    /**
     * Feature order is defined by {@link ClusteringFeatures}.
     */
    public static double[] vectorize(Customer c) {
        double tenure = c.tenure();
        double monthly = c.monthlyCharges();
        double total = c.totalCharges();
        double senior = c.seniorCitizen();

        double contractLen = contractLength(c.contract());
        double serviceCount = serviceCount(c);
        double paperless = yesNo(c.paperlessBilling());
        double fiber = isFiber(c.internetService()) ? 1.0 : 0.0;

        return new double[] {
                tenure,
                monthly,
                total,
                senior,
                contractLen,
                serviceCount,
                paperless,
                fiber
        };
    }

    public static int dimension() {
        return ClusteringFeatures.values().length;
    }

    private static double contractLength(String contract) {
        if (contract == null) return 0.0;
        String v = contract.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "month-to-month" -> 0.0;
            case "one year" -> 1.0;
            case "two year" -> 2.0;
            default -> 0.0;
        };
    }

    private static double yesNo(String s) {
        if (s == null) return 0.0;
        return s.trim().equalsIgnoreCase("Yes") ? 1.0 : 0.0;
    }

    private static boolean isFiber(String internetService) {
        if (internetService == null) return false;
        return internetService.trim().equalsIgnoreCase("Fiber optic");
    }

    /**
     * Rough proxy for "how many services" a customer uses.
     * This tends to separate: minimal users vs power users.
     */
    private static double serviceCount(Customer c) {
        int count = 0;
        // Phone + multi lines
        if ("Yes".equalsIgnoreCase(c.phoneService())) count++;
        if ("Yes".equalsIgnoreCase(c.multipleLines())) count++;

        // Internet itself counts as a service.
        if (c.internetService() != null && !c.internetService().equalsIgnoreCase("No")) count++;

        // Add-ons
        if ("Yes".equalsIgnoreCase(c.onlineSecurity())) count++;
        if ("Yes".equalsIgnoreCase(c.onlineBackup())) count++;
        if ("Yes".equalsIgnoreCase(c.deviceProtection())) count++;
        if ("Yes".equalsIgnoreCase(c.techSupport())) count++;
        if ("Yes".equalsIgnoreCase(c.streamingTV())) count++;
        if ("Yes".equalsIgnoreCase(c.streamingMovies())) count++;

        return count;
    }
}
