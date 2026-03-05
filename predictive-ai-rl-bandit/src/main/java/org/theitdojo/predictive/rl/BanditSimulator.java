package org.theitdojo.predictive.rl;

import org.theitdojo.predictive.core.Customer;

import java.util.EnumMap;
import java.util.Map;

public final class BanditSimulator {

    public record Snapshot(int step,
                           double banditAvgReward,
                           double randomAvgReward,
                           double banditAcceptanceRate,
                           Map<OfferAction, Double> banditOfferRates) {}

    private BanditSimulator() {}

    public static BanditSimulationResult run(int steps,
                                             BanditPolicy bandit,
                                             Policy randomPolicy,
                                             RewardModel env,
                                             TelecomCustomerStream stream,
                                             int snapshotEvery,
                                             java.util.function.Consumer<Snapshot> onSnapshot) {

        double banditReward = 0.0;
        double randomReward = 0.0;

        int banditAccepted = 0;
        int randomAccepted = 0;

        Map<OfferAction, Integer> banditChosen = new EnumMap<>(OfferAction.class);
        for (OfferAction a : OfferAction.values()) banditChosen.put(a, 0);

        for (int i = 1; i <= steps; i++) {
            Customer c = stream.nextCustomer(i);

            // Bandit
            OfferAction a1 = bandit.choose(c);
            banditChosen.put(a1, banditChosen.get(a1) + 1);

            RewardOutcome o1 = env.evaluate(c, a1);
            bandit.update(c, a1, o1.reward());

            banditReward += o1.reward();
            if (o1.accepted()) banditAccepted++;

            // Random baseline (same stream, same env)
            OfferAction a2 = randomPolicy.choose(c);
            RewardOutcome o2 = env.evaluate(c, a2);

            randomReward += o2.reward();
            if (o2.accepted()) randomAccepted++;

            if (snapshotEvery > 0 && i % snapshotEvery == 0) {
                Map<OfferAction, Double> rates = new EnumMap<>(OfferAction.class);
                for (OfferAction a : OfferAction.values()) {
                    rates.put(a, banditChosen.get(a) / (double) i);
                }
                onSnapshot.accept(new Snapshot(
                        i,
                        banditReward / i,
                        randomReward / i,
                        banditAccepted / (double) i,
                        rates
                ));
            }
        }

        return new BanditSimulationResult(
                steps,
                banditReward / steps,
                randomReward / steps,
                banditAccepted / (double) steps,
                randomAccepted / (double) steps
        );
    }
}
