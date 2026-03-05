package org.theitdojo.predictive.inference;

import org.theitdojo.predictive.core.Customer;
import org.theitdojo.predictive.core.Predictor;

import java.util.ArrayList;
import java.util.List;

public final class BenchmarkRunner {

    private BenchmarkRunner() {}

    public static BenchmarkResult run(Predictor predictor, int n) throws Exception {

        List<Long> latencies = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Customer c = RandomCustomerGenerator.randomCustomer(i);
            var p = predictor.predict(c);
            latencies.add(p.latencyMs());
        }

        return BenchmarkResult.from(latencies);
    }
}
