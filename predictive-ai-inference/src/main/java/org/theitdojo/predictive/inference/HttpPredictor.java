package org.theitdojo.predictive.inference;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.theitdojo.predictive.core.Customer;
import org.theitdojo.predictive.core.Feature;
import org.theitdojo.predictive.core.Prediction;
import org.theitdojo.predictive.core.Predictor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for the Python sidecar API.
 */
public final class HttpPredictor implements Predictor {

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final URI endpoint;

    public HttpPredictor(URI endpoint) {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.endpoint = endpoint;
    }

    @Override
    public Prediction predict(Customer c) throws Exception {
        long start = System.nanoTime();
        Map<String, Object> payload = toPayload(c);
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        long end = System.nanoTime();

        long latency = (end - start) / 1_000_000;

        @SuppressWarnings("unchecked")
        Map<String, Object> result = mapper.readValue(response.body(), Map.class);

        Object pred = result.get("prediction");
        Object conf = result.get("confidence");

        String label = (pred instanceof String s && !s.isBlank()) ? s : "UNKNOWN";
        float confidence = (conf instanceof Number n) ? n.floatValue() : 0.0f;

        return new Prediction(label, confidence, latency);
    }

    private static Map<String, Object> toPayload(Customer c) {
        Map<String, Object> m = new HashMap<>();

        m.put(Feature.GENDER.key(), c.gender());
        m.put(Feature.SENIOR_CITIZEN.key(), c.seniorCitizen());
        m.put(Feature.PARTNER.key(), c.partner());
        m.put(Feature.DEPENDENTS.key(), c.dependents());
        m.put(Feature.TENURE.key(), c.tenure());
        m.put(Feature.PHONE_SERVICE.key(), c.phoneService());
        m.put(Feature.MULTIPLE_LINES.key(), c.multipleLines());
        m.put(Feature.INTERNET_SERVICE.key(), c.internetService());
        m.put(Feature.ONLINE_SECURITY.key(), c.onlineSecurity());
        m.put(Feature.ONLINE_BACKUP.key(), c.onlineBackup());
        m.put(Feature.DEVICE_PROTECTION.key(), c.deviceProtection());
        m.put(Feature.TECH_SUPPORT.key(), c.techSupport());
        m.put(Feature.STREAMING_TV.key(), c.streamingTV());
        m.put(Feature.STREAMING_MOVIES.key(), c.streamingMovies());
        m.put(Feature.CONTRACT.key(), c.contract());
        m.put(Feature.PAPERLESS_BILLING.key(), c.paperlessBilling());
        m.put(Feature.PAYMENT_METHOD.key(), c.paymentMethod());
        m.put(Feature.MONTHLY_CHARGES.key(), c.monthlyCharges());
        m.put(Feature.TOTAL_CHARGES.key(), c.totalCharges());

        return m;
    }
}
