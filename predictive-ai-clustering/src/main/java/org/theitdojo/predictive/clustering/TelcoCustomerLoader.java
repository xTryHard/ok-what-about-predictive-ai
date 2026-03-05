package org.theitdojo.predictive.clustering;

import org.theitdojo.predictive.core.Customer;
import org.theitdojo.predictive.core.ResourceFiles;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the Telco churn CSV into {@link Customer} records.
 *
 * We reuse the same dataset already present in the project resources.
 */
public final class TelcoCustomerLoader {

    private TelcoCustomerLoader() {}

    public static List<Customer> loadFromClasspath(String csvClasspath) throws Exception {
        Path csv = ResourceFiles.copyToTempFile(csvClasspath, ".csv");

        try (BufferedReader br = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String header = br.readLine();
            if (header == null) throw new IllegalStateException("CSV is empty");

            List<String> headers = parseCsvLine(header);
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                idx.put(headers.get(i), i);
            }

            List<Customer> out = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> row = parseCsvLine(line);
                // Defensive: some rows may have fewer columns due to malformed TotalCharges.
                if (row.size() < headers.size()) {
                    // Pad missing values.
                    while (row.size() < headers.size()) row.add("");
                }
                out.add(mapRow(row, idx));
            }
            return out;
        }
    }

    private static Customer mapRow(List<String> r, Map<String, Integer> i) {
        String id = get(r, i, "customerID");
        String gender = get(r, i, "gender");
        float senior = parseFloat(get(r, i, "SeniorCitizen"));
        String partner = get(r, i, "Partner");
        String dependents = get(r, i, "Dependents");
        float tenure = parseFloat(get(r, i, "tenure"));
        String phoneService = get(r, i, "PhoneService");
        String multipleLines = get(r, i, "MultipleLines");
        String internetService = get(r, i, "InternetService");
        String onlineSecurity = get(r, i, "OnlineSecurity");
        String onlineBackup = get(r, i, "OnlineBackup");
        String deviceProtection = get(r, i, "DeviceProtection");
        String techSupport = get(r, i, "TechSupport");
        String streamingTV = get(r, i, "StreamingTV");
        String streamingMovies = get(r, i, "StreamingMovies");
        String contract = get(r, i, "Contract");
        String paperless = get(r, i, "PaperlessBilling");
        String paymentMethod = get(r, i, "PaymentMethod");
        float monthlyCharges = parseFloat(get(r, i, "MonthlyCharges"));
        float totalCharges = parseFloat(get(r, i, "TotalCharges"));
        String churn = get(r, i, "Churn");

        return new Customer(
                id, gender, senior,
                partner, dependents,
                tenure,
                phoneService, multipleLines, internetService,
                onlineSecurity, onlineBackup, deviceProtection, techSupport,
                streamingTV, streamingMovies,
                contract, paperless, paymentMethod,
                monthlyCharges, totalCharges,
                churn
        );
    }

    private static String get(List<String> row, Map<String, Integer> idx, String key) {
        Integer pos = idx.get(key);
        if (pos == null || pos >= row.size()) return "";
        return row.get(pos);
    }

    private static float parseFloat(String s) {
        if (s == null) return 0f;
        s = s.trim();
        if (s.isEmpty()) return 0f;
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            // TotalCharges sometimes contains blanks/spaces.
            return 0f;
        }
    }

    /**
     * Minimal CSV parser that handles quoted commas.
     * (Good enough for the telco dataset.)
     */
    static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int p = 0; p < line.length(); p++) {
            char c = line.charAt(p);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            sb.append(c);
        }
        out.add(sb.toString());
        return out;
    }
}
