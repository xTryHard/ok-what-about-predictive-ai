package org.theitdojo.predictive.djl.exception;

public class UnsupportedModeException extends Exception {

    private final String mode;

    public UnsupportedModeException(String mode) {
        super("Unsupported processing mode: \"" + mode + "\". Valid options are: 1 (Image), 2 (Video).");
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
