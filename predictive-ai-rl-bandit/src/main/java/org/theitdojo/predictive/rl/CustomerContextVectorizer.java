package org.theitdojo.predictive.rl;

import org.theitdojo.predictive.core.Customer;

import java.util.Locale;

/**
 * Converts a Customer record into a small numeric feature vector.
 * Keep this small & explainable for stage.
 */
public final class CustomerContextVectorizer {

    private CustomerContextVectorizer() {}

    // Feature dimension
    public static final int D = 10;

    public static double[] toVector(Customer c) {
        double[] x = new double[D];

        // 0) Bias
        x[0] = 1.0;

        // 1) Tenure (scaled to 0..1-ish)
        x[1] = clamp(c.tenure() / 72.0);

        // 2) MonthlyCharges (scaled to 0..1-ish)
        x[2] = clamp(c.monthlyCharges() / 120.0);

        // 3) SeniorCitizen (0/1)
        x[3] = c.seniorCitizen() >= 1 ? 1.0 : 0.0;

        // 4-6) Contract one-hot: month-to-month, one year, two year
        String contract = norm(c.contract());
        x[4] = contract.contains("month") ? 1.0 : 0.0;
        x[5] = contract.contains("one") ? 1.0 : 0.0;
        x[6] = contract.contains("two") ? 1.0 : 0.0;

        // 7) Fiber optic flag
        String internet = norm(c.internetService());
        x[7] = internet.contains("fiber") ? 1.0 : 0.0;

        // 8) Paperless billing
        x[8] = "yes".equals(norm(c.paperlessBilling())) ? 1.0 : 0.0;

        // 9) TechSupport
        x[9] = "yes".equals(norm(c.techSupport())) ? 1.0 : 0.0;

        return x;
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }

    private static double clamp(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
