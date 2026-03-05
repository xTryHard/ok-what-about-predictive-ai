package org.theitdojo.predictive.tensorflow;

import org.theitdojo.predictive.tensorflow.detector.EfficientDetDetector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.theitdojo.predictive.tensorflow.ModelResolver.resolveModelDir;

/**
 * Benchmarks TensorFlow EfficientDet inference throughput and latency.
 * Runs detection on a single test image N times and reports per-frame statistics.
 */
public final class BenchmarkRunner {

    private BenchmarkRunner() {}

    public static void run(Scanner scanner) throws Exception {
        System.out.println("\n=== EfficientDet (TensorFlow Java) Benchmark ===");

        System.out.print("Enter test image path: ");
        String imagePath = scanner.nextLine().trim();

        System.out.print("How many inference runs? (e.g. 100): ");
        int n;
        try {
            n = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }
        if (n <= 0) {
            System.out.println("Number must be > 0.");
            return;
        }

        Path path = Paths.get(imagePath).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            System.out.println("File not found: " + path);
            return;
        }

        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null) {
            System.out.println("Could not decode image: " + path);
            return;
        }

        System.out.println("\nLoading EfficientDet SavedModel...");
        String modelDir = resolveModelDir();

        try (EfficientDetDetector detector = new EfficientDetDetector(modelDir)) {
            System.out.println("Model loaded. Warming up (5 runs)...");
            for (int i = 0; i < 5; i++) {
                detector.detect(image);
            }

            System.out.println("Running benchmark (" + n + " runs)...\n");
            List<Long> latencies = new ArrayList<>(n);
            long totalStart = System.currentTimeMillis();

            for (int i = 0; i < n; i++) {
                long start = System.nanoTime();
                detector.detect(image);
                latencies.add((System.nanoTime() - start) / 1_000_000); // ms
            }

            long totalMs = System.currentTimeMillis() - totalStart;
            printReport(latencies, totalMs);
        }
    }

    // Model resolution delegated to ModelResolver

    private static void printReport(List<Long> latencies, long totalMs) {
        Collections.sort(latencies);

        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        long avg = sum / latencies.size();
        long min = latencies.getFirst();
        long max = latencies.getLast();
        long p95 = latencies.get((int) (latencies.size() * 0.95));
        double fps = latencies.size() / (totalMs / 1000.0);

        System.out.println("=".repeat(45));
        System.out.println(" TensorFlow Benchmark Results — EfficientDet");
        System.out.println("=".repeat(45));
        System.out.printf(" Runs        : %d%n", latencies.size());
        System.out.printf(" Avg latency : %d ms%n", avg);
        System.out.printf(" Min latency : %d ms%n", min);
        System.out.printf(" Max latency : %d ms%n", max);
        System.out.printf(" p95 latency : %d ms%n", p95);
        System.out.printf(" Throughput  : %.1f FPS%n", fps);
        System.out.println("=".repeat(45));
    }
}
