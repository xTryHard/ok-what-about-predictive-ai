package org.theitdojo.predictive.demos;

import org.theitdojo.predictive.rl.*;

import java.util.Random;
import java.util.Scanner;

public final class RLBanditDemoMenu {

    /**
     * Single, visual RL demo:
     * - Contextual Bandit (LinUCB) vs Random baseline
     * - Prints snapshots as it learns
     */
    public void run(Scanner scanner) throws Exception {

        System.out.println("\n====================================");
        System.out.println(" RL DEMO: ADAPTIVE OFFER OPTIMIZATION");
        System.out.println("====================================");

        int steps = askInt(scanner,
                "How many decisions to simulate? (e.g., 500)",
                500);

        int snapshotEvery = Math.max(25, steps / 10);

        // Seeded for stage stability (repeatable learning curve)
        Random rng = new Random(42);

        var env = new SyntheticTelecomRewardModel(rng);
        var stream = new TelecomCustomerStream(rng);

        var bandit = new TribuoBanditPolicy(
                0.10,   // epsilon exploration
                30,     // min samples before training per action
                10,     // retrain frequency per action
                rng
        );

        var randomPolicy = new RandomPolicy(rng);

        System.out.println("\nRunning... snapshots every " + snapshotEvery + " steps\n");

        BanditSimulationResult result = BanditSimulator.run(
                steps,
                bandit,
                randomPolicy,
                env,
                stream,
                snapshotEvery,
                snap -> {
                    System.out.println("Step " + snap.step());
                    System.out.printf("  Bandit  avg reward: %.3f | acceptance: %.1f%%%n",
                            snap.banditAvgReward(), snap.banditAcceptanceRate() * 100);
                    System.out.printf("  Random  avg reward: %.3f | acceptance: ---%n",
                            snap.randomAvgReward());
                    System.out.println("  Bandit offer distribution:");
                    printOfferBars(snap.banditOfferRates());
                    System.out.println();
                }
        );

        System.out.println("====================================");
        System.out.println(" FINAL RESULT");
        System.out.println("====================================");
        System.out.printf("Bandit avg reward: %.3f | acceptance: %.1f%%%n",
                result.banditAvgReward(), result.banditAcceptanceRate() * 100);
        System.out.printf("Random avg reward: %.3f | acceptance: %.1f%%%n",
                result.randomAvgReward(), result.randomAcceptanceRate() * 100);

        double lift = (result.banditAvgReward() - result.randomAvgReward()) / Math.max(1e-9, result.randomAvgReward()) * 100.0;

        System.out.printf("Lift vs Random: %.1f%%%n", lift);

        System.out.println("====================================");
        System.out.println("Press Enter to return to main menu...");
        scanner.nextLine();
    }

    private static int askInt(Scanner scanner, String prompt, int fallback) {
        System.out.print(prompt + " [" + fallback + "]: ");
        String s = scanner.nextLine().trim();
        if (s.isBlank()) return fallback;
        try {
            int v = Integer.parseInt(s);
            return v > 0 ? v : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static void printOfferBars(java.util.Map<OfferAction, Double> rates) {
        for (OfferAction a : OfferAction.values()) {
            double r = rates.getOrDefault(a, 0.0);
            int blocks = (int) Math.round(r * 20); // 20-char bar
            String bar = "█".repeat(Math.max(0, blocks)) + "░".repeat(Math.max(0, 20 - blocks));
            System.out.printf("    %-14s %s  (%.0f%%)%n", a.name(), bar, r * 100);
        }
    }
}
