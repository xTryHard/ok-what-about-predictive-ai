import tensorflow as tf
import numpy as np
from PIL import Image, ImageDraw, ImageFont
import requests
from io import BytesIO

# 1. Load the EfficientDet SavedModel
model = tf.saved_model.load("src/main/resources/models/efficientdet")
detect_fn = model.signatures["serving_default"]

# 2. Use a sample image URL
image_url = "https://techcrunch.com/wp-content/uploads/2018/03/mlb-youtube-tv-home-plate.png?w=640"

# 3. Download and decode the image
response = requests.get(image_url)
img = Image.open(BytesIO(response.content)).convert("RGB")
img_np = np.array(img)

# 4. Run inference
input_tensor = tf.convert_to_tensor(img_np[np.newaxis, ...], dtype=tf.uint8)
detections = detect_fn(input_tensor=input_tensor)

# 5. Extract results
scores = detections["detection_scores"].numpy()[0]
classes = detections["detection_classes"].numpy()[0].astype(int)
boxes = detections["detection_boxes"].numpy()[0]
num = int(detections["num_detections"].numpy()[0])

# COCO labels (subset)
COCO_LABELS = {
    1: "person", 2: "bicycle", 3: "car", 4: "motorcycle", 5: "airplane",
    6: "bus", 7: "train", 8: "truck", 9: "boat", 10: "traffic light",
    37: "sports ball", 39: "baseball bat", 40: "baseball glove",
}

threshold = 0.3
print("--- Detection Results ---")
count = 0
for i in range(num):
    if scores[i] >= threshold:
        count += 1
        label = COCO_LABELS.get(classes[i], f"class_{classes[i]}")
        print(f"  {count}) {label} — confidence: {scores[i]*100:.2f}%")

if count == 0:
    print("  No objects detected above threshold.")
print("-------------------------")

# 6. Save annotated image
draw = ImageDraw.Draw(img)
h, w = img_np.shape[:2]
for i in range(num):
    if scores[i] >= threshold:
        ymin, xmin, ymax, xmax = boxes[i]
        draw.rectangle([xmin*w, ymin*h, xmax*w, ymax*h], outline="lime", width=3)
        label = COCO_LABELS.get(classes[i], f"class_{classes[i]}")
        draw.text((xmin*w, ymin*h - 12), f"{label} {scores[i]*100:.1f}%", fill="lime")

img.save("python_inference_result.jpg")
print("Annotated image saved as python_inference_result.jpg")
