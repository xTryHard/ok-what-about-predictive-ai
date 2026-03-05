package org.theitdojo.predictive.rl;

import org.theitdojo.predictive.core.Customer;
import org.tribuo.*;
import org.tribuo.datasource.ListDataSource;
import org.tribuo.impl.ListExample;
import org.tribuo.math.optimisers.AdaGrad;
import org.tribuo.provenance.SimpleDataSourceProvenance;
import org.tribuo.regression.RegressionFactory;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.sgd.linear.LinearSGDTrainer;
import org.tribuo.regression.sgd.objectives.SquaredLoss;

import java.util.*;

/**
 * Contextual Bandit policy backed by Tribuo regression models (one model per action).
 *
 * Intuition:
 * - We treat "expected reward" as a regression problem: r = f(context, action)
 * - Maintain a small online dataset per action and retrain periodically (fast, demo-friendly)
 * - Choose the action with the highest predicted reward (with epsilon exploration)
 *
 * This uses Tribuo so the demo shows a real Java ML library, not hand-rolled math.
 */
public final class TribuoBanditPolicy implements BanditPolicy {

    private static final String REWARD_DIM = "reward";

    private final double epsilon;
    private final Random random;

    private final int minTrainSize;
    private final int retrainEvery;

    private final RegressionFactory factory = new RegressionFactory();
    private final Trainer<Regressor> trainer;

    private final Map<OfferAction, List<Example<Regressor>>> data = new EnumMap<>(OfferAction.class);
    private final Map<OfferAction, Model<Regressor>> models = new EnumMap<>(OfferAction.class);
    private final Map<OfferAction, Integer> updates = new EnumMap<>(OfferAction.class);

    public TribuoBanditPolicy(double epsilon, int minTrainSize, int retrainEvery, Random random) {
        this.epsilon = epsilon;
        this.minTrainSize = minTrainSize;
        this.retrainEvery = retrainEvery;
        this.random = random;

        // Simple, stable trainer for stage demos.
        this.trainer = new LinearSGDTrainer(new SquaredLoss(), new AdaGrad(0.1), 10, 42L);

        for (OfferAction a : OfferAction.values()) {
            data.put(a, new ArrayList<>());
            models.put(a, null);
            updates.put(a, 0);
        }
    }

    @Override
    public OfferAction choose(Customer customer) {

        // Exploration: choose random sometimes.
        if (random.nextDouble() < epsilon) {
            OfferAction[] actions = OfferAction.values();
            return actions[random.nextInt(actions.length)];
        }

        // If we don't have models yet, fall back to random to collect data.
        boolean anyModel = models.values().stream().anyMatch(Objects::nonNull);
        if (!anyModel) {
            OfferAction[] actions = OfferAction.values();
            return actions[random.nextInt(actions.length)];
        }

        double[] x = CustomerContextVectorizer.toVector(customer);

        OfferAction best = OfferAction.values()[0];
        double bestScore = Double.NEGATIVE_INFINITY;

        for (OfferAction a : OfferAction.values()) {

            Model<Regressor> m = models.get(a);
            if (m == null) {
                // No model yet for this action, lightly encourage exploration.
                double jitter = random.nextDouble() * 0.01;
                if (jitter > bestScore) {
                    bestScore = jitter;
                    best = a;
                }
                continue;
            }

            Example<Regressor> ex = exampleFor(x, 0.0);
            Prediction<Regressor> pred = m.predict(ex);

            double score = pred.getOutput().getValues()[0];

            if (score > bestScore) {
                bestScore = score;
                best = a;
            }
        }

        return best;
    }

    @Override
    public void update(Customer customer, OfferAction action, double reward) {
        double[] x = CustomerContextVectorizer.toVector(customer);

        data.get(action).add(exampleFor(x, reward));
        int u = updates.get(action) + 1;
        updates.put(action, u);

        if (data.get(action).size() >= minTrainSize && (u % retrainEvery == 0)) {
            retrain(action);
        }
    }

    private void retrain(OfferAction action) {
        List<Example<Regressor>> examples = data.get(action);

        var provenance = new SimpleDataSourceProvenance("bandit-" + action.name(), factory);
        var ds = new ListDataSource<>(examples, factory, provenance);
        var dataset = new MutableDataset<>(ds);

        Model<Regressor> model = trainer.train(dataset);
        models.put(action, model);
    }

    private static Example<Regressor> exampleFor(double[] x, double reward) {
        String[] names = new String[x.length];
        double[] values = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            names[i] = "f" + i;
            values[i] = x[i];
        }
        Regressor y = new Regressor(REWARD_DIM, reward);
        return new ListExample<>(y, names, values);
    }
}
