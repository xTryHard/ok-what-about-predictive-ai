package org.theitdojo.predictive.rl;

import org.theitdojo.predictive.core.Customer;

/**
 * A Policy that can learn online (i.e., gets feedback/reward after choosing an action).
 */
public interface BanditPolicy extends Policy {
    void update(Customer customer, OfferAction action, double reward);
}
