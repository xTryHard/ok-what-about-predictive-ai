package org.theitdojo.predictive.djl;

import org.theitdojo.predictive.djl.exception.UnsupportedModeException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Shared CLI prompts for the tracking runners (bat, baseball, combined).
 */
final class TrackingPrompts {

    private TrackingPrompts() {}

    static String promptMode(Scanner scanner) {
        while (true) {
            System.out.print("Select mode (1 = Image, 2 = Video): ");
            String mode = scanner.nextLine().trim();
            try {
                if (!mode.equals("1") && !mode.equals("2")) throw new UnsupportedModeException(mode);
                return mode;
            } catch (UnsupportedModeException e) {
                System.out.println("Invalid mode \"" + mode + "\". Please enter 1 or 2.");
            }
        }
    }

    static String promptInput(Scanner scanner) {
        System.out.print("Enter input file path or URL: ");
        return scanner.nextLine().trim();
    }

    static Path promptOutput(Scanner scanner, String mode, String prefix) throws Exception {
        String defaultOutput = mode.equals("1") ? prefix + "-output.png" : prefix + "-output.mp4";
        System.out.print("Enter output file path (default: " + defaultOutput + "): ");
        String outputRaw = scanner.nextLine().trim();
        if (outputRaw.isEmpty()) outputRaw = defaultOutput;

        Path output = Paths.get(System.getProperty("user.dir")).resolve(outputRaw).toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        return output;
    }
}
