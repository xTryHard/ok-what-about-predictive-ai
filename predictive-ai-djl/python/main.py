from ultralytics import YOLO
import cv2
import requests
import numpy as np

# 1. Load the PyTorch model
# Use the path to your trained .pt file
model = YOLO("models/bat_tracking/bat_tracking.pt")

# 2. Use the exact same image URL from your Java project
image_url = "https://techcrunch.com/wp-content/uploads/2018/03/mlb-youtube-tv-home-plate.png?w=640"

# 3. Load the image into memory once to avoid stream errors
response = requests.get(image_url)
nparr = np.frombuffer(response.content, np.uint8)
img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

# 4. Run inference
# imgsz=640 matches the resize we are doing in the Java Translator
results = model.predict(source=img, imgsz=640, conf=0.25)

# 5. Extract and display the confidence
for result in results:
    # Result.boxes.conf contains the confidence scores
    if len(result.boxes.conf) > 0:
        conf_percentage = result.boxes.conf[0].item() * 100
        print(f"--> Python Bat Confidence: {conf_percentage:.2f}%")
    else:
        print("--> No bat detected in Python.")

# 6. Save the annotated image to verify the bounding box
annotated_img = results[0].plot()
cv2.imwrite("python_inference_result.jpg", annotated_img)
print("Annotated image saved as python_inference_result.jpg")

