package org.theitdojo.predictive.rl;

import org.theitdojo.predictive.core.Customer;

import java.util.Locale;
import java.util.Random;

/**
 * Stage-safe synthetic reward model:
 * - Computes a simple "risk" score from customer fields.
 * - Maps (customer, offer) to P(accept).
 * - Samples acceptance.
 *
 * Goal: bandit learns meaningful patterns quickly (hundreds of steps).
 */
public final class SyntheticTelecomRewardModel implements RewardModel {

    private final Random random;

    public SyntheticTelecomRewardModel(Random random) {
        this.random = random;
    }

    @Override
    public RewardOutcome evaluate(Customer c, OfferAction action) {

        double risk = churnRiskProxy(c);

        double p = switch (action) {
            case DISCOUNT_10 -> clamp(0.12 + 0.70 * risk);
            case DISCOUNT_5 -> clamp(0.10 + 0.55 * risk);
            case FREE_ADDON -> clamp(0.08 + 0.35 * risk + (isFiber(c) ? 0.05 : 0.0));
            case LOYALTY_POINTS -> clamp(0.10 + 0.35 * (1.0 - risk) + (c.tenure() >= 24 ? 0.05 : 0.0));
        };

        boolean accepted = random.nextDouble() < p;

        // Keep reward simple & explainable (accepted=1, rejected=0)
        double reward = accepted ? 1.0 : 0.0;

        return new RewardOutcome(accepted, reward, p);
    }

    private static double churnRiskProxy(Customer c) {
        double r = 0.0;

        // Contract
        String contract = norm(c.contract());
        if (contract.contains("month")) r += 0.25;
        if (contract.contains("two")) r -= 0.08;

        // Tenure
        if (c.tenure() < 12) r += 0.20;
        else if (c.tenure() > 48) r -= 0.12;

        // Monthly charges
        if (c.monthlyCharges() > 80) r += 0.18;
        else if (c.monthlyCharges() < 35) r -= 0.05;

        // Internet type
        if (isFiber(c)) r += 0.08;

        // Support / security can lower risk
        if ("Yes".equalsIgnoreCase(c.techSupport())) r -= 0.05;
        if ("Yes".equalsIgnoreCase(c.onlineSecurity())) r -= 0.05;

        // Paperless billing tends to correlate slightly with higher churn in many telco datasets
        if ("Yes".equalsIgnoreCase(c.paperlessBilling())) r += 0.04;

        return clamp(r);
    }

    private static boolean isFiber(Customer c) {
        return norm(c.internetService()).contains("fiber");
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }

    private static double clamp(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}
