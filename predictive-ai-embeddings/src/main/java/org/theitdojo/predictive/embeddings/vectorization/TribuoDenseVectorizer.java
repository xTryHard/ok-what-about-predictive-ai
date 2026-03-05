package org.theitdojo.predictive.embeddings.vectorization;


import org.tribuo.Example;
import org.tribuo.Feature;
import org.tribuo.ImmutableFeatureMap;

public final class TribuoDenseVectorizer implements ExampleVectorizer {

    @Override
    public double[] vectorize(Example<?> example, ImmutableFeatureMap featureMap) {
        int dim = featureMap.size();
        double[] v = new double[dim];

        for (Feature f : example) {
            int id = featureMap.getID(f.getName());
            if (id >= 0 && id < dim) {
                v[id] = f.getValue();
            }
        }

        l2NormalizeInPlace(v);
        return v;
    }

    private static void l2NormalizeInPlace(double[] v) {
        double norm = 0.0;
        for (double x : v) norm += x * x;
        norm = Math.sqrt(norm);
        if (norm == 0.0) return;
        for (int i = 0; i < v.length; i++) v[i] /= norm;
    }
}
