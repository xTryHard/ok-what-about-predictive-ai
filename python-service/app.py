from fastapi import FastAPI
import onnxruntime as ort
import numpy as np

app = FastAPI()

session = ort.InferenceSession("churn_model.onnx")

# IMPORTANT: same mapping used during training
LABELS = {0: "NO CHURN", 1: "CHURN"}

@app.post("/predict")
def predict(data: dict):

    inputs = {}

    # numeric
    for k in ["SeniorCitizen", "tenure", "MonthlyCharges", "TotalCharges"]:
        inputs[k] = np.array([[float(data[k])]], dtype=np.float32)

    # categorical
    for k in data:
        if k not in inputs:
            inputs[k] = np.array([[str(data[k])]], dtype=object)

    outputs = session.run(None, inputs)

    pred_idx = int(outputs[0][0])

    prob_map = outputs[1][0]

    confidence = float(prob_map[pred_idx])

    return {
        "prediction": LABELS[pred_idx],
        "confidence": confidence
    }
