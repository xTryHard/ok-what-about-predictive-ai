package org.theitdojo.predictive.rl;

import org.theitdojo.predictive.core.Customer;

import java.util.Random;

/**
 * Generates plausible Telco-like customer contexts for the RL demo.
 * (No dependency on predictive-ai-inference fixtures.)
 */
public final class TelecomCustomerStream {

    private final Random random;

    public TelecomCustomerStream(Random random) {
        this.random = random;
    }

    public Customer nextCustomer(int id) {

        boolean fiber = random.nextDouble() < 0.45;
        boolean monthToMonth = random.nextDouble() < 0.55;

        float tenure = (float) (random.nextDouble() * 72.0);
        float monthly = (float) (20.0 + random.nextDouble() * 100.0);

        String contract = monthToMonth ? "Month-to-month" : (random.nextBoolean() ? "One year" : "Two year");

        String internetService = fiber ? "Fiber optic" : (random.nextDouble() < 0.45 ? "DSL" : "No");

        String techSupport = random.nextDouble() < (fiber ? 0.35 : 0.45) ? "Yes" : "No";
        String onlineSecurity = random.nextDouble() < (fiber ? 0.30 : 0.45) ? "Yes" : "No";

        return new Customer(
                "RL-" + id,
                random.nextBoolean() ? "Male" : "Female",
                random.nextDouble() < 0.15 ? 1 : 0,
                random.nextBoolean() ? "Yes" : "No",
                random.nextDouble() < 0.30 ? "Yes" : "No",
                tenure,
                random.nextDouble() < 0.85 ? "Yes" : "No",
                random.nextDouble() < 0.35 ? "Yes" : "No",
                internetService,
                onlineSecurity,
                random.nextDouble() < 0.45 ? "Yes" : "No",
                random.nextDouble() < 0.45 ? "Yes" : "No",
                techSupport,
                random.nextDouble() < 0.50 ? "Yes" : "No",
                random.nextDouble() < 0.50 ? "Yes" : "No",
                contract,
                random.nextDouble() < 0.70 ? "Yes" : "No",
                random.nextDouble() < 0.55 ? "Electronic check" : "Mailed check",
                monthly,
                monthly * tenure, // not realistic but fine for demo context
                null
        );
    }
}
