package org.theitdojo.predictive.djl;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import org.theitdojo.predictive.djl.detector.ObjectDetector;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Benchmarks DJL object detection inference throughput and latency.
 * Runs detection on a single test image N times and reports per-frame statistics.
 */
public final class BenchmarkRunner {

    private BenchmarkRunner() {}

    public static void run(Scanner scanner) throws Exception {
        System.out.println("\n=== DJL Object Detection Benchmark ===");
        System.out.println("Select model:");
        System.out.println("  1) Bat detection (YOLOv5)");
        System.out.println("  2) Baseball detection (YOLOv11)");
        System.out.print("Choice: ");
        String modelChoice = scanner.nextLine().trim();

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

        Path path = Path.of(imagePath).toAbsolutePath().normalize();
        Image image = ImageFactory.getInstance().fromFile(path);

        System.out.println("\nLoading model...");
        try (ObjectDetector detector = "2".equals(modelChoice)
                ? ObjectDetector.forBaseball()
                : ObjectDetector.forBats()) {

            System.out.println("Model loaded. Warming up (10 runs)...");
            for (int i = 0; i < 10; i++) {
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
            printReport(latencies, totalMs, "2".equals(modelChoice) ? "Baseball (YOLOv11)" : "Bat (YOLOv5)");
        }
    }

    private static void printReport(List<Long> latencies, long totalMs, String modelName) {
        Collections.sort(latencies);

        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        long avg = sum / latencies.size();
        long min = latencies.getFirst();
        long max = latencies.getLast();
        long p95 = latencies.get((int) (latencies.size() * 0.95));
        double fps = latencies.size() / (totalMs / 1000.0);

        System.out.println("=".repeat(45));
        System.out.println(" DJL Benchmark Results — " + modelName);
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
