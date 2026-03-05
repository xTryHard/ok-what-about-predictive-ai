package org.theitdojo.predictive.tensorflow;

import java.util.Scanner;

/**
 * Standalone entry point for running EfficientDet detection directly:
 * <pre>
 *   mvn -q exec:java -pl predictive-ai-tensorflow \
 *       -Dexec.mainClass=org.theitdojo.predictive.tensorflow.EfficientDetDemo
 * </pre>
 */
public class EfficientDetDemo {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        EfficientDetRunner.run(scanner);
    }
}
