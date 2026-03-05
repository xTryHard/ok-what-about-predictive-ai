package org.theitdojo.predictive.djl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Benchmarks object detection via a remote Python FastAPI service.
 * Measures full round-trip: HTTP POST + server-side inference + response.
 */
public final class RemoteBenchmarkRunner {

    private static final String DEFAULT_URL = "http://localhost:8001/detect";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RemoteBenchmarkRunner() {}

    public static void run(Scanner scanner) throws Exception {
        System.out.println("\n=== Remote Python Service Benchmark ===");

        System.out.print("Service URL (default: " + DEFAULT_URL + "): ");
        String urlInput = scanner.nextLine().trim();
        String baseUrl = urlInput.isEmpty() ? DEFAULT_URL : urlInput;

        System.out.println("Select model:");
        System.out.println("  1) Bat detection (YOLOv5)");
        System.out.println("  2) Baseball detection (YOLOv11)");
        System.out.print("Choice: ");
        String modelChoice = scanner.nextLine().trim();
        String modelKey = "2".equals(modelChoice) ? "baseball" : "bat";

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

        // Read and base64-encode the image once
        Path path = Path.of(imagePath).toAbsolutePath().normalize();
        byte[] imageBytes = Files.readAllBytes(path);
        String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
        String payload = MAPPER.writeValueAsString(Map.of("image", imageBase64));

        URI endpoint = URI.create(baseUrl + "/" + modelKey);

        try (HttpClient client = HttpClient.newHttpClient()) {
            // Verify service is reachable before starting benchmark
            System.out.println("\nConnecting to " + endpoint + " ...");
            try {
                sendRequest(client, endpoint, payload);
            } catch (ConnectException | HttpConnectTimeoutException e) {
                throw new IOException(
                        "Cannot connect to detection service at " + endpoint
                        + "\nMake sure the Python service is running:"
                        + "\n  cd python-detection-service && python app.py");
            }

            System.out.println("Service is up. Warming up (10 runs)...");
            for (int i = 0; i < 9; i++) {
                sendRequest(client, endpoint, payload);
            }

            System.out.println("Running benchmark (" + n + " runs)...\n");
            List<Long> latencies = new ArrayList<>(n);
            long totalStart = System.currentTimeMillis();

            for (int i = 0; i < n; i++) {
                long start = System.nanoTime();
                sendRequest(client, endpoint, payload);
                latencies.add((System.nanoTime() - start) / 1_000_000);
            }

            long totalMs = System.currentTimeMillis() - totalStart;
            String modelName = "2".equals(modelChoice) ? "Baseball (YOLOv11)" : "Bat (YOLOv5)";
            printReport(latencies, totalMs, modelName);
        }
    }

    private static void sendRequest(HttpClient client, URI endpoint, String payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Detection service returned HTTP " + response.statusCode()
                    + ": " + response.body());
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

        System.out.println("=".repeat(50));
        System.out.println(" Remote Benchmark Results — " + modelName);
        System.out.println("=".repeat(50));
        System.out.printf(" Runs        : %d%n", latencies.size());
        System.out.printf(" Avg latency : %d ms (full round-trip)%n", avg);
        System.out.printf(" Min latency : %d ms%n", min);
        System.out.printf(" Max latency : %d ms%n", max);
        System.out.printf(" p95 latency : %d ms%n", p95);
        System.out.printf(" Throughput  : %.1f FPS%n", fps);
        System.out.println("=".repeat(50));
    }
}
