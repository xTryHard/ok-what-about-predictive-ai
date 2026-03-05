package org.theitdojo.predictive.rl;

import org.theitdojo.predictive.core.Customer;

public interface Policy {
    OfferAction choose(Customer customer);
}
