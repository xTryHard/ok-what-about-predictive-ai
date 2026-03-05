package org.theitdojo.predictive.rl;

public record BanditSimulationResult(
        int steps,
        double banditAvgReward,
        double randomAvgReward,
        double banditAcceptanceRate,
        double randomAcceptanceRate
) {}
