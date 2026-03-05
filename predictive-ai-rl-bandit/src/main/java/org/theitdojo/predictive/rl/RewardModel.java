package org.theitdojo.predictive.rl;

import org.theitdojo.predictive.core.Customer;

/**
 * "Environment" for the bandit demo.
 * Given a customer context and an action (offer), returns a reward (e.g., accepted=1.0).
 */
public interface RewardModel {
    RewardOutcome evaluate(Customer customer, OfferAction action);
}
