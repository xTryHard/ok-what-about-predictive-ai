package org.theitdojo.predictive.core;

/**
 * Single source of truth for model features.
 *
 * Keys must match:
 * - Tribuo CSV column names
 * - ONNX model input tensor names
 * - Python sidecar API payload keys
 */
public enum Feature {
    // Numeric
    SENIOR_CITIZEN("SeniorCitizen", Type.NUMERIC),
    TENURE("tenure", Type.NUMERIC),
    MONTHLY_CHARGES("MonthlyCharges", Type.NUMERIC),
    TOTAL_CHARGES("TotalCharges", Type.NUMERIC),

    // Categorical
    GENDER("gender", Type.CATEGORICAL),
    PARTNER("Partner", Type.CATEGORICAL),
    DEPENDENTS("Dependents", Type.CATEGORICAL),
    PHONE_SERVICE("PhoneService", Type.CATEGORICAL),
    MULTIPLE_LINES("MultipleLines", Type.CATEGORICAL),
    INTERNET_SERVICE("InternetService", Type.CATEGORICAL),
    ONLINE_SECURITY("OnlineSecurity", Type.CATEGORICAL),
    ONLINE_BACKUP("OnlineBackup", Type.CATEGORICAL),
    DEVICE_PROTECTION("DeviceProtection", Type.CATEGORICAL),
    TECH_SUPPORT("TechSupport", Type.CATEGORICAL),
    STREAMING_TV("StreamingTV", Type.CATEGORICAL),
    STREAMING_MOVIES("StreamingMovies", Type.CATEGORICAL),
    CONTRACT("Contract", Type.CATEGORICAL),
    PAPERLESS_BILLING("PaperlessBilling", Type.CATEGORICAL),
    PAYMENT_METHOD("PaymentMethod", Type.CATEGORICAL);

    public enum Type { NUMERIC, CATEGORICAL }

    private final String key;
    private final Type type;

    Feature(String key, Type type) {
        this.key = key;
        this.type = type;
    }

    public String key() {
        return key;
    }

    public Type type() {
        return type;
    }
}
