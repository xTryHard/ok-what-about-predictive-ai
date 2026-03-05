package org.theitdojo.predictive.clustering;

/**
 * A 2D point (after PCA projection) with its cluster assignment.
 */
public record ClusterPoint2D(double x, double y, int clusterId) {}
