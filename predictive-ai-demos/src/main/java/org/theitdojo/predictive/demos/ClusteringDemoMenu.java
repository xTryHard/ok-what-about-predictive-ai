package org.theitdojo.predictive.demos;

import org.theitdojo.predictive.clustering.ClusteringPipeline;

import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

/**
 * Customer segmentation demo:
 * K-Means clustering (Tribuo) + PCA projection + PNG scatter plot.
 */
public final class ClusteringDemoMenu {

    public void run(Scanner scanner) throws Exception {
        System.out.println("\n====================================");
        System.out.println(" CUSTOMER SEGMENTATION (CLUSTERING) ");
        System.out.println("====================================");
        System.out.println("We will:");
        System.out.println("  1) Cluster customers with K-Means (Tribuo)");
        System.out.println("  2) Project to 2D with PCA");
        System.out.println("  3) Render a PNG scatter plot colored by cluster");
        System.out.println("  4) Automatically label each cluster\n");

        int k = askInt(scanner, "How many clusters (k)? [default: 4] ", 4);
        int maxPoints = askInt(scanner, "How many points to plot? [default: 1200] ", 1200);
        String out = askString(scanner, "Output image path? [default: customer_clusters.png] ", "customer_clusters.png");

        var pipeline = new ClusteringPipeline(42L);
        var result = pipeline.run("/telco-customer-churn.csv", k, maxPoints, Path.of(out));

        System.out.println("\n✅ Saved plot: " + result.imagePath().toAbsolutePath());

        System.out.println("\nCluster summary:");
        for (Map.Entry<Integer, Integer> e : result.clusterSizes().entrySet()) {
            int clusterId = e.getKey();
            int size = e.getValue();
            String label = result.clusterLabels().getOrDefault(clusterId, "Unknown");

            System.out.printf(
                    "  Cluster %d (%s): %d customers%n",
                    clusterId,
                    label,
                    size
            );
        }

        System.out.println();
    }

    private static int askInt(Scanner s, String prompt, int def) {
        System.out.print(prompt);
        String in = s.nextLine().trim();
        if (in.isEmpty()) return def;
        try {
            return Integer.parseInt(in);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String askString(Scanner s, String prompt, String def) {
        System.out.print(prompt);
        String in = s.nextLine().trim();
        return in.isEmpty() ? def : in;
    }
}
