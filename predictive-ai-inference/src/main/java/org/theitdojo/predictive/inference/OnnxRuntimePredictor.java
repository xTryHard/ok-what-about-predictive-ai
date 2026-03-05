package org.theitdojo.predictive.inference;

import ai.onnxruntime.*;
import org.theitdojo.predictive.core.Customer;
import org.theitdojo.predictive.core.Feature;
import org.theitdojo.predictive.core.Prediction;
import org.theitdojo.predictive.core.Predictor;
import org.theitdojo.predictive.core.ResourceFiles;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for local ONNX Runtime inference.
 */
public final class OnnxRuntimePredictor implements Predictor {

    private final OrtEnvironment env;
    private final OrtSession session;

    public OnnxRuntimePredictor(Path modelPath) throws Exception {
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(modelPath.toString(), new OrtSession.SessionOptions());
    }

    public static OnnxRuntimePredictor fromClasspath(String classpathModel) throws Exception {
        Path p = ResourceFiles.copyToTempFile(classpathModel, ".onnx");
        return new OnnxRuntimePredictor(p);
    }

    @Override
    public Prediction predict(Customer c) throws Exception {
        long start = System.nanoTime();
        Map<String, OnnxTensor> inputs = new HashMap<>();

        inputs.put(Feature.SENIOR_CITIZEN.key(), num(c.seniorCitizen()));
        inputs.put(Feature.TENURE.key(), num(c.tenure()));
        inputs.put(Feature.MONTHLY_CHARGES.key(), num(c.monthlyCharges()));
        inputs.put(Feature.TOTAL_CHARGES.key(), num(c.totalCharges()));

        inputs.put(Feature.GENDER.key(), str(c.gender()));
        inputs.put(Feature.PARTNER.key(), str(c.partner()));
        inputs.put(Feature.DEPENDENTS.key(), str(c.dependents()));
        inputs.put(Feature.PHONE_SERVICE.key(), str(c.phoneService()));
        inputs.put(Feature.MULTIPLE_LINES.key(), str(c.multipleLines()));
        inputs.put(Feature.INTERNET_SERVICE.key(), str(c.internetService()));
        inputs.put(Feature.ONLINE_SECURITY.key(), str(c.onlineSecurity()));
        inputs.put(Feature.ONLINE_BACKUP.key(), str(c.onlineBackup()));
        inputs.put(Feature.DEVICE_PROTECTION.key(), str(c.deviceProtection()));
        inputs.put(Feature.TECH_SUPPORT.key(), str(c.techSupport()));
        inputs.put(Feature.STREAMING_TV.key(), str(c.streamingTV()));
        inputs.put(Feature.STREAMING_MOVIES.key(), str(c.streamingMovies()));
        inputs.put(Feature.CONTRACT.key(), str(c.contract()));
        inputs.put(Feature.PAPERLESS_BILLING.key(), str(c.paperlessBilling()));
        inputs.put(Feature.PAYMENT_METHOD.key(), str(c.paymentMethod()));

        OrtSession.Result results = session.run(inputs);
        long end = System.nanoTime();

        long latency = (end - start) / 1_000_000;

        long[] predicted = (long[]) results.get(0).getValue();

        @SuppressWarnings("unchecked")
        List<OnnxMap> probList = (List<OnnxMap>) results.get(1).getValue();

        OnnxMap onnxMap = probList.getFirst();

        Map<Long, Float> probs = new HashMap<>();
        for (Map.Entry<?, ?> entry : onnxMap.getValue().entrySet()) {
            probs.put((Long) entry.getKey(), (Float) entry.getValue());
        }

        int labelIdx = (int) predicted[0];
        float confidence = probs.getOrDefault((long) labelIdx, 0.0f);

        // This matches your current convention.
        String label = labelIdx == 1 ? "CHURN" : "NO CHURN";

        return new Prediction(label, confidence, latency);
    }

    private OnnxTensor num(float v) throws Exception {
        return OnnxTensor.createTensor(env, new float[][]{{v}});
    }

    private OnnxTensor str(String v) throws Exception {
        return OnnxTensor.createTensor(env, new String[][]{{v}});
    }
}
