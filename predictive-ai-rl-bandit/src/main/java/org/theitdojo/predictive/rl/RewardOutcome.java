package org.theitdojo.predictive.rl;

public record RewardOutcome(boolean accepted, double reward, double acceptanceProbability) {}
