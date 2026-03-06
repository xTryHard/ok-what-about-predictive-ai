package org.theitdojo.predictive.clustering;

import org.apache.commons.math3.linear.RealMatrix;
import org.theitdojo.predictive.core.Customer;

import java.nio.file.Path;
import java.util.*;

public final class ClusteringPipeline {

    public record Result(
            Path imagePath,
            int k,
            int points,
            Map<Integer, Integer> clusterSizes,
            Map<Integer, String> clusterLabels
    ) {}

    private final KMeansClusteringService kmeans;

    public ClusteringPipeline(long seed) {
        this.kmeans = new KMeansClusteringService(seed);
    }

    public Result run(String telcoCsvClasspath, int k, int maxPoints, Path outputImage) throws Exception {
        List<Customer> customers = TelcoCustomerLoader.loadFromClasspath(telcoCsvClasspath);

        var result = kmeans.cluster(customers, k, maxPoints);
        var clusteredPoints = result.points();
        var centroids = result.centroids();

        // -----------------------------
        // Build matrix for PCA (points)
        // -----------------------------
        double[][] vectors = new double[clusteredPoints.size()][];
        int[] clusterIds = new int[clusteredPoints.size()];

        for (int i = 0; i < clusteredPoints.size(); i++) {
            vectors[i] = clusteredPoints.get(i).vector();
            clusterIds[i] = clusteredPoints.get(i).clusterId();
        }

        // -----------------------------
        // FIT PCA ON POINTS (ONCE)
        // -----------------------------
        Pca2D pca = Pca2D.fit(vectors);

        double[][] xy = pca.transform(vectors);

        // -----------------------------
        // Project centroids USING SAME PCA
        // -----------------------------
        double[][] centroidVectors = centroids.values().toArray(new double[0][]);
        double[][] centroidXY = pca.transform(centroidVectors);

        Map<Integer, ClusterPoint2D> centroidPoints = new HashMap<>();
        int i = 0;
        for (Integer cid : centroids.keySet()) {
            centroidPoints.put(
                    cid,
                    new ClusterPoint2D(
                            centroidXY[i][0],
                            centroidXY[i][1],
                            cid
                    )
            );
            i++;
        }

        // -----------------------------
        // Build 2D points
        // -----------------------------
        List<ClusterPoint2D> points2d = new ArrayList<>(xy.length);
        for (int j = 0; j < xy.length; j++) {
            points2d.add(new ClusterPoint2D(xy[j][0], xy[j][1], clusterIds[j]));
        }

        // -----------------------------
        // Cluster sizes
        // -----------------------------
        Map<Integer, Integer> sizes = new HashMap<>();
        for (int cid : clusterIds) {
            sizes.merge(cid, 1, Integer::sum);
        }

        // -----------------------------
        // Automatic labels
        // -----------------------------
        System.out.println("\nCluster centroids (feature means):");
        for (var e : centroids.entrySet()) {
            int cid = e.getKey();
            double[] c = e.getValue();

            System.out.println("\nCluster " + cid);
            for (ClusteringFeatures f : ClusteringFeatures.values()) {
                System.out.printf("%-20s %.2f%n", f.name(), c[f.ordinal()]);
            }
        }

        Map<Integer, String> labels = getLabels(centroids);

        // -----------------------------
        // Principal components loading
        // -----------------------------
        System.out.println("\nPrincipal components:");
        RealMatrix components = pca.getComponents();

        for (int pc = 0; pc < 2; pc++) {
            System.out.println("\nPC" + (pc + 1));
            for (ClusteringFeatures f : ClusteringFeatures.values()) {
                int idx = f.ordinal();
                double value = components.getEntry(pc, idx);
                System.out.printf("%-20s %.4f%n", f.name(), value);
            }
        }

        // -----------------------------
        // Render
        // -----------------------------
        Path saved = ClusterPlotRenderer.renderPng(
                points2d,
                centroidPoints,
                labels,
                k,
                outputImage
        );

        return new Result(saved, k, points2d.size(), sizes, labels);
    }

    private static Map<Integer, String> getLabels(Map<Integer, double[]> centroids) {
        Map<Integer, String> labels = new HashMap<>();

        for (var e : centroids.entrySet()) {
            int cid = e.getKey();
            double[] c = e.getValue();

            double total = c[ClusteringFeatures.TOTAL_CHARGES.ordinal()];

            String label;

            if (total < 800) {
                label = "New / Low Engagement";
            } else if (total < 3000) {
                label = "Core Customers";
            } else if (total < 5500) {
                label = "High-Value Customers";
            } else {
                label = "Premium Power Users";
            }

            labels.put(cid, label);
        }
        return labels;
    }
}
