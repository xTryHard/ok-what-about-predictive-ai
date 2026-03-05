package org.theitdojo.predictive.inference;

import org.theitdojo.predictive.core.Customer;

import java.util.Random;

public final class RandomCustomerGenerator {

    private static final Random R = new Random();

    private RandomCustomerGenerator() {}

    public static Customer randomCustomer(int id) {

        return new Customer(
                "BENCH-" + id,
                R.nextBoolean() ? "Male" : "Female",
                R.nextBoolean() ? 1 : 0,
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Yes" : "No",
                R.nextInt(72),
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Fiber optic" : "DSL",
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Month-to-month" : "One year",
                R.nextBoolean() ? "Yes" : "No",
                R.nextBoolean() ? "Electronic check" : "Mailed check",
                R.nextFloat() * 120f,
                R.nextFloat() * 5000f,
                R.nextBoolean() ? "Yes" : "No"
        );
    }
}
