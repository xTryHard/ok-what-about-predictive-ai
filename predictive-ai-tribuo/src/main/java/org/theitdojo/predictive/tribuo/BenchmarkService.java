package org.theitdojo.predictive.tribuo;

import org.tribuo.Dataset;
import org.tribuo.Model;
import org.tribuo.Trainer;
import org.tribuo.classification.Label;
import org.tribuo.classification.evaluation.LabelEvaluation;
import org.tribuo.classification.evaluation.LabelEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BenchmarkService {

    public record ModelResult(String name, LabelEvaluation eval, long timeMs) {}

    public List<ModelResult> run(Map<String, Trainer<Label>> trainers, Dataset<Label> train, Dataset<Label> test) {
        System.out.println("\n=== Running Benchmarks (Weighted Training) ===");

        var results = new ArrayList<ModelResult>();
        var evaluator = new LabelEvaluator();

        for (var entry : trainers.entrySet()) {
            long start = System.currentTimeMillis();
            Model<Label> model = entry.getValue().train(train);
            LabelEvaluation eval = evaluator.evaluate(model, test);
            results.add(new ModelResult(entry.getKey(), eval, System.currentTimeMillis() - start));
        }
        return results;
    }

    public void printReport(List<ModelResult> results) {
        System.out.println("\n" + "=".repeat(75));
        System.out.printf("%-20s | %10s | %12s | %10s | %10s%n", "Algorithm", "Accuracy", "Recall (Yes)", "F1 (Yes)", "Time");
        System.out.println("-".repeat(75));

        for (var r : results) {
            Optional<Label> yesLabel = r.eval.getConfusionMatrix().getLabelOrder().stream()
                    .filter(l -> l.getLabel().equalsIgnoreCase("Yes"))
                    .findFirst();

            double recall = yesLabel.map(r.eval::recall).orElse(0.0);
            double f1 = yesLabel.map(r.eval::f1).orElse(0.0);

            System.out.printf("%-20s | %9.2f%% | %11.2f%% | %9.2f%% | %8.2fs%n",
                    r.name, r.eval.accuracy() * 100, recall * 100, f1 * 100, r.timeMs / 1000.0);
        }
        System.out.println("=".repeat(75));

        for (var r : results) {
            System.out.println("\n--- Detailed Analysis: " + r.name + " ---");
            System.out.println("Confusion Matrix:");
            System.out.println(r.eval.getConfusionMatrix());
            System.out.println("Full Evaluation Metrics:");
            System.out.println(r.eval);
            System.out.println("-".repeat(40));
        }
    }
}
