package org.theitdojo.predictive.demos;

import org.theitdojo.predictive.djl.BaseballTrackingRunner;
import org.theitdojo.predictive.djl.BatTrackingRunner;
import org.theitdojo.predictive.djl.CombinedTrackingRunner;
import org.theitdojo.predictive.tensorflow.EfficientDetRunner;

import java.util.Scanner;

/**
 * One entrypoint for stage demos.
 */
public class MainMenu {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n====================================");
            System.out.println(" PREDICTIVE AI DEVNEXUS DEMOS");
            System.out.println("====================================");
            System.out.println("1) Tribuo Training + Evaluation");
            System.out.println("2) Inference (Local ONNX / Python API)");
            System.out.println("3) Bat Tracking (YOLOv5 ONNX via DJL)");
            System.out.println("4) Embeddings / Vector Search (KNN)");
            System.out.println("5) EfficientDet (TensorFlow Java)");
            System.out.println("6) Baseball Tracking (YOLOv11 ONNX via DJL)");
            System.out.println("7) Combined Tracking (Bat + Baseball)");
            System.out.println("8) RL: Adaptive Offer Optimization (Bandit)");
            System.out.println("9) Clustering: Customer Segments (K-Means + PCA)");
            System.out.println("10) Exit");
            System.out.print("\nEnter choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> new TribuoDemoMenu().run(scanner);
                    case "2" -> new InferenceDemoMenu().run(scanner);
                    case "3" -> BatTrackingRunner.run(scanner);
                    case "4" -> new EmbeddingsDemoMenu().run(scanner);
                    case "5" -> EfficientDetRunner.run(scanner);
                    case "6" -> BaseballTrackingRunner.run(scanner);
                    case "7" -> CombinedTrackingRunner.run(scanner);
                    case "8" -> new RLBanditDemoMenu().run(scanner);
                    case "9" -> new ClusteringDemoMenu().run(scanner);
                    case "10" -> {
                        System.out.println("\nGoodbye");
                        return;
                    }
                    default -> System.out.println("Unknown command.");
                }
            } catch (Exception e) {
                System.out.println("\nERROR:");
                String msg = e.getMessage();
                if (msg == null || msg.isBlank()) {
                    System.out.println(e.getClass().getSimpleName() + " (no details available)");
                } else {
                    System.out.println(msg);
                }
                System.out.println("Returning to menu...");
            }
        }
    }
}
