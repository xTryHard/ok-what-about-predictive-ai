package org.theitdojo.predictive.rl;

import org.theitdojo.predictive.core.Customer;

import java.util.Random;

public final class RandomPolicy implements Policy {

    private final Random random;

    public RandomPolicy(Random random) {
        this.random = random;
    }

    @Override
    public OfferAction choose(Customer customer) {
        OfferAction[] actions = OfferAction.values();
        return actions[random.nextInt(actions.length)];
    }
}
