package org.theitdojo.predictive.core;

public interface Predictor {
    Prediction predict(Customer customer) throws Exception;
}
