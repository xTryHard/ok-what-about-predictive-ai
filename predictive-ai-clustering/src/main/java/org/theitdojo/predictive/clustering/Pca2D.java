package org.theitdojo.predictive.clustering;

import org.apache.commons.math3.linear.*;

public final class Pca2D {

    private final RealVector mean;        // size = numFeatures
    private final RealMatrix components;  // shape = 2 x numFeatures

    private Pca2D(RealVector mean, RealMatrix components) {
        this.mean = mean;
        this.components = components;
    }

    /** Fit PCA on the given data matrix (rows = samples, cols = features) */
    public static Pca2D fit(double[][] data) {
        RealMatrix X = MatrixUtils.createRealMatrix(data);

        int nSamples = X.getRowDimension();
        int nFeatures = X.getColumnDimension();

        // 1. Compute mean per feature
        double[] meanArr = new double[nFeatures];
        for (int j = 0; j < nFeatures; j++) {
            double sum = 0.0;
            for (int i = 0; i < nSamples; i++) {
                sum += X.getEntry(i, j);
            }
            meanArr[j] = sum / nSamples;
        }
        RealVector mean = MatrixUtils.createRealVector(meanArr);

        // 2. Center data
        for (int i = 0; i < nSamples; i++) {
            X.setRowVector(i, X.getRowVector(i).subtract(mean));
        }

        // 3. Covariance matrix
        RealMatrix cov =
                X.transpose()
                        .multiply(X)
                        .scalarMultiply(1.0 / (nSamples - 1));

        // 4. Eigen decomposition
        EigenDecomposition eig = new EigenDecomposition(cov);
        RealMatrix V = eig.getV(); // columns = eigenvectors

        // 5. Take top 2 eigenvectors
        RealMatrix components =
                V.getSubMatrix(0, nFeatures - 1, 0, 1).transpose();

        return new Pca2D(mean, components);
    }

    /** Project data into 2D PCA space using fitted model */
    public double[][] transform(double[][] data) {
        RealMatrix X = MatrixUtils.createRealMatrix(data);

        for (int i = 0; i < X.getRowDimension(); i++) {
            X.setRowVector(i, X.getRowVector(i).subtract(mean));
        }

        return X.multiply(components.transpose()).getData();
    }

    public RealMatrix getComponents() {
        return components;
    }
}
