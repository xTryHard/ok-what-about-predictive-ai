package org.theitdojo.predictive.embeddings.vectorization;

import org.tribuo.Example;
import org.tribuo.ImmutableFeatureMap;

/**
 * Turns a Tribuo Example into a dense, normalized vector aligned with the dataset feature map.
 *
 * This is the "embedding" in this demo: a feature vector.
 */
public interface ExampleVectorizer {
    double[] vectorize(Example<?> example, ImmutableFeatureMap featureMap);
}
