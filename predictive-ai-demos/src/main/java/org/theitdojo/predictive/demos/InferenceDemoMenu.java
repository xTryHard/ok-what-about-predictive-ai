package org.theitdojo.predictive.demos;

import org.theitdojo.predictive.core.Customer;
import org.theitdojo.predictive.core.Prediction;
import org.theitdojo.predictive.core.Predictor;
import org.theitdojo.predictive.inference.*;

import java.net.URI;
import java.util.List;
import java.util.Scanner;

public final class InferenceDemoMenu {

    private final String churnApiUrl;

    public InferenceDemoMenu() {
        this.churnApiUrl = System.getProperty(
                "churn.api.url",
                "http://localhost:9999/predict"  // default fallback
        );
    }

    public void run(Scanner scanner) throws Exception {

        while (true) {
            System.out.println("\n====================================");
            System.out.println(" TELECOM CHURN INFERENCE DEMO");
            System.out.println("====================================");
            System.out.println("1) Local ONNX Runtime");
            System.out.println("2) Python API Service");
            System.out.println("3) Run Benchmark");
            System.out.println("4) Back");
            System.out.print("\nChoice: ");

            String choice = scanner.nextLine().trim();

            if ("4".equals(choice)) return;

            if ("3".equals(choice)) {
                runBenchmark(scanner);
                continue;
            }

            try {
                int c = Integer.parseInt(choice);
                runInference(c);
            } catch (NumberFormatException nfe) {
                System.out.println("Unknown command.");
            }
        }
    }

    private void runInference(int choice) throws Exception {

        List<Customer> tests = CustomerFixtures.smokeTestCustomers();

        long totalLatency = 0;
        int correct = 0;

        System.out.println("\nRUNNING INFERENCE...\n");

        Predictor predictor;

        if (choice == 1) {
            predictor = OnnxRuntimePredictor.fromClasspath("/churn_model.onnx");
        } else if (choice == 2) {
            predictor = new HttpPredictor(
                    URI.create(churnApiUrl)
            );
        } else {
            System.out.println("Unknown command.");
            return;
        }

        for (Customer c : tests) {
            Prediction p = predictor.predict(c);
            printResult(c, p);
            totalLatency += p.latencyMs();
            if (isCorrect(c, p)) correct++;
        }

        System.out.println("\n====================================");
        System.out.printf("Accuracy: %.2f%%%n", (float) correct / tests.size() * 100);
        System.out.println("Avg Latency: " + totalLatency / tests.size() + " ms");
        System.out.println("====================================");
    }

    private void runBenchmark(Scanner scanner) throws Exception {

        System.out.print("\nHow many customers to generate? ");
        String input = scanner.nextLine().trim();

        int N;

        try {
            N = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }

        if (N <= 0) {
            System.out.println("Number must be > 0.");
            return;
        }

        Predictor local = OnnxRuntimePredictor.fromClasspath("/churn_model.onnx");
        Predictor remote = new HttpPredictor(URI.create(this.churnApiUrl));

        System.out.println("\nWarming up...");
        BenchmarkRunner.run(local, 20);
        BenchmarkRunner.run(remote, 5);

        System.out.println("\nRunning benchmark with " + N + " customers...\n");

        BenchmarkResult localResult = BenchmarkRunner.run(local, N);
        BenchmarkResult remoteResult = BenchmarkRunner.run(remote, N);

        printBenchmark("Local ONNX", localResult);
        printBenchmark("Remote HTTP", remoteResult);
    }

    private void printBenchmark(String name, BenchmarkResult r) {
        System.out.println("====================================");
        System.out.println(name);
        System.out.println("====================================");
        System.out.println("Avg Latency: " + r.avg() + " ms");
        System.out.println("Min Latency: " + r.min() + " ms");
        System.out.println("Max Latency: " + r.max() + " ms");
        System.out.println("p95 Latency: " + r.p95() + " ms");
        System.out.println();
    }

    private static void printResult(Customer c, Prediction p) {
        System.out.println("===================================");
        System.out.println("Customer:   " + c.id());
        System.out.println("Actual:     " + c.actual());
        System.out.println("Predicted:  " + p.label());
        System.out.printf("Confidence: %.2f%%%n", p.confidence() * 100);
        System.out.println("Latency:    " + p.latencyMs() + " ms");
    }

    private static boolean isCorrect(Customer c, Prediction p) {
        String pred = p.label() == null ? "UNKNOWN" : p.label();
        String actual = c.actual() == null ? "" : c.actual();

        return ("CHURN".equals(pred) && actual.equalsIgnoreCase("Yes"))
                || ("NO CHURN".equals(pred) && actual.equalsIgnoreCase("No"));
    }
}
