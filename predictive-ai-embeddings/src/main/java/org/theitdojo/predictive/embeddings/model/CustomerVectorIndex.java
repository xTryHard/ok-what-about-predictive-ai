package org.theitdojo.predictive.embeddings.model;

import org.theitdojo.predictive.core.Customer;

import java.util.List;
import java.util.Map;

public record CustomerVectorIndex(
        List<Customer> customers,
        List<double[]> vectors,
        Map<String, Integer> idToIndex
) {
    public int size() { return customers.size(); }

    public Customer customerById(String id) {
        Integer idx = idToIndex.get(id);
        if (idx == null) return null;
        return customers.get(idx);
    }

    public double[] vectorById(String id) {
        Integer idx = idToIndex.get(id);
        if (idx == null) return null;
        return vectors.get(idx);
    }
}
