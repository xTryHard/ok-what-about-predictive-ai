package org.theitdojo.predictive.tribuo;

import org.tribuo.Trainer;
import org.tribuo.classification.Label;
import org.tribuo.classification.dtree.CARTClassificationTrainer;
import org.tribuo.classification.ensemble.VotingCombiner;
import org.tribuo.classification.sgd.linear.LinearSGDTrainer;
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer;
import org.tribuo.classification.sgd.objectives.Hinge;
import org.tribuo.common.tree.RandomForestTrainer;
import org.tribuo.math.optimisers.AdaGrad;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TrainerRegistry {
    private TrainerRegistry() {}

    public static Map<String, Trainer<Label>> defaultTrainers() {
        var trainers = new LinkedHashMap<String, Trainer<Label>>();

        trainers.put("Logistic Regression", new LogisticRegressionTrainer());
        trainers.put("Linear SVM", new LinearSGDTrainer(new Hinge(), new AdaGrad(0.1), 5, 42L));

        var treeTrainer = new CARTClassificationTrainer(12, 0.3f, false, 42L);
        trainers.put("Random Forest", new RandomForestTrainer<>(treeTrainer, new VotingCombiner(), 50, 42L));

        return trainers;
    }
}
