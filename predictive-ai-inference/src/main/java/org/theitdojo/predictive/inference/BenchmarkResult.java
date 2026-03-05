package org.theitdojo.predictive.inference;

import java.util.Collections;
import java.util.List;

public record BenchmarkResult(long avg, long min, long max, long p95) {

    public static BenchmarkResult from(List<Long> latencies) {

        Collections.sort(latencies);

        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        long avg = sum / latencies.size();
        long min = latencies.getFirst();
        long max = latencies.getLast();

        int idx95 = (int)(latencies.size() * 0.95);
        long p95 = latencies.get(idx95);

        return new BenchmarkResult(avg, min, max, p95);
    }
}
