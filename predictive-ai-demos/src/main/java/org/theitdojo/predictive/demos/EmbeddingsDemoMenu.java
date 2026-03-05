package org.theitdojo.predictive.demos;

import org.theitdojo.predictive.embeddings.model.CustomerVectorIndex;
import org.theitdojo.predictive.embeddings.service.CustomerVectorSearchService;

import java.util.Scanner;

public final class EmbeddingsDemoMenu {

    private final CustomerVectorSearchService service = new CustomerVectorSearchService();
    private CustomerVectorIndex index;

    public void run(Scanner scanner) throws Exception {
        if (index == null) {
            System.out.println("\nLoading dataset + building in-memory vector index (KNN)...");
            index = service.buildIndex("/telco-customer-churn.csv");
            System.out.println("Index ready. Customers indexed: " + index.size());
        }

        while (true) {
            System.out.println("\n--- Embeddings / Vector Search Demo Menu ---");
            System.out.println("1) Similar customers for a HIGH-risk profile (fixture)");
            System.out.println("2) Similar customers for a LOW-risk profile (fixture)");
            System.out.println("3) Enter a customerId and run vector search");
            System.out.println("4) Back");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> runForId("7892-POOKP", 5);
                case "2" -> runForId("5575-GNVDE", 5);
                case "3" -> {
                    System.out.print("Enter customerId (e.g. 7590-VHVEG): ");
                    String id = scanner.nextLine().trim();
                    runForId(id, 5);
                }
                case "4" -> { return; }
                default -> System.out.println("Unknown command.");
            }
        }
    }

    private void runForId(String id, int k) {
        var c = index.customerById(id);
        if (c == null) {
            System.out.println("Customer id not found: " + id);
            return;
        }
        System.out.println("\nQuery customer:");
        System.out.println(CustomerVectorSearchService.shortProfile(c));
        printNeighborhood(id, k);
    }

    private void printNeighborhood(String id, int k) {
        var analysis = service.analyzeNeighborhood(index, id, k);
        var neighbors = service.findSimilar(index, id, k);

        System.out.println("\nTop-" + k + " similar customers (cosine similarity):");
        System.out.println("------------------------------------------------------------");
        for (int i = 0; i < neighbors.size(); i++) {
            var r = neighbors.get(i);
            System.out.printf("%d) sim=%.3f  %s%n", i + 1, r.similarity(), CustomerVectorSearchService.shortProfile(r.item()));
        }
        System.out.println("------------------------------------------------------------");
        System.out.printf("Neighborhood churn mix: Yes=%d, No=%d | avgSim=%.3f | risk=%s%n",
                analysis.churnYes(), analysis.churnNo(), analysis.averageSimilarity(), analysis.riskSignal());
    }
}
