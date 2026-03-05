package org.theitdojo.predictive.djl.processor;

import java.nio.file.Path;

/**
 * Processes a media input (image, video, …) through the detection pipeline
 * and writes an annotated result to the given output path.
 */
public interface MediaProcessor {
    void process(String input, Path output) throws Exception;
}
