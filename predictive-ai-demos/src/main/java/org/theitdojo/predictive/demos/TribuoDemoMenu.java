package org.theitdojo.predictive.demos;

import org.theitdojo.predictive.tribuo.*;

import java.io.File;
import java.util.Scanner;

public final class TribuoDemoMenu {

    public void run(Scanner scanner) throws Exception {

        var datasetFactory = new ChurnDatasetFactory("/telco-customer-churn.csv", 0.7, 42L);
        var split = datasetFactory.split();

        var trainers = TrainerRegistry.defaultTrainers();
        var eda = new EdaService();
        var bench = new BenchmarkService();
        var prov = new ProvenanceExporter();

        while (true) {
            System.out.println("\n--- Tribuo Demo Menu ---");
            System.out.println("1) Run EDA (Full Data Source)");
            System.out.println("2) Run Model Benchmark (Train & Evaluate)");
            System.out.println("3) Export Deep Provenance (JSON)");
            System.out.println("4) Back");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> eda.printFullSourceProfile(split.source());
                case "2" -> {
                    var results = bench.run(trainers, split.train(), split.test());
                    bench.printReport(results);
                }
                case "3" -> prov.exportAll(trainers, split.train(), new File("provenance-exports"));
                case "4" -> { return; }
                default -> System.out.println("Unknown command.");
            }
        }
    }
}
