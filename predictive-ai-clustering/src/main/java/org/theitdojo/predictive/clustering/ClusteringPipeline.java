package org.theitdojo.predictive.clustering;

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
        Map<Integer, String> labels = new HashMap<>();
        for (var e : centroids.entrySet()) {
            int cid = e.getKey();
            double[] c = e.getValue();

            double tenure = c[ClusteringFeatures.TENURE.ordinal()];
            double charges = c[ClusteringFeatures.MONTHLY_CHARGES.ordinal()];

            String label;
            if (tenure > 40 && charges > 70) {
                label = "Loyal High-Value";
            } else if (tenure < 12 && charges > 70) {
                label = "New High-Risk";
            } else if (tenure > 40) {
                label = "Stable Long-Term";
            } else {
                label = "Price-Sensitive";
            }

            labels.put(cid, label);
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
}
