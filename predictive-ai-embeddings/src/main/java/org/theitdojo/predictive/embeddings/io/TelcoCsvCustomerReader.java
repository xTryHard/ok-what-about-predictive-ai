package org.theitdojo.predictive.embeddings.io;

import org.theitdojo.predictive.core.Customer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the Telco Customer Churn CSV into Customer records.
 * Keeps the original file order so we can align with Tribuo DataSource rows.
 */
public final class TelcoCsvCustomerReader {

    public List<Customer> read(Path csvPath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String header = br.readLine(); // skip header
            if (header == null) return List.of();

            List<Customer> out = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                // The telco dataset does not contain commas inside fields,
                // so a simple split works for this demo.
                String[] c = line.split(",", -1);
                if (c.length < 21) continue;

                out.add(new Customer(
                        c[0],                        // id
                        c[1],                        // gender
                        parseFloat(c[2]),            // SeniorCitizen
                        c[3],                        // Partner
                        c[4],                        // Dependents
                        parseFloat(c[5]),            // tenure
                        c[6],                        // PhoneService
                        c[7],                        // MultipleLines
                        c[8],                        // InternetService
                        c[9],                        // OnlineSecurity
                        c[10],                       // OnlineBackup
                        c[11],                       // DeviceProtection
                        c[12],                       // TechSupport
                        c[13],                       // StreamingTV
                        c[14],                       // StreamingMovies
                        c[15],                       // Contract
                        c[16],                       // PaperlessBilling
                        c[17],                       // PaymentMethod
                        parseFloat(c[18]),           // MonthlyCharges
                        parseFloat(c[19]),           // TotalCharges (may be blank)
                        c[20]                        // Churn (actual)
                ));
            }
            return out;
        }
    }

    private static float parseFloat(String s) {
        if (s == null) return 0f;
        s = s.trim();
        if (s.isEmpty()) return 0f;
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }
}
