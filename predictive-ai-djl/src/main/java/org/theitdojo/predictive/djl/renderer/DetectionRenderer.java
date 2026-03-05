package org.theitdojo.predictive.djl.renderer;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.List;

public class DetectionRenderer {

    private static final Color BOX_COLOR = new Color(0, 255, 0);
    private static final Color LABEL_BG = new Color(0, 200, 0, 180);
    private static final Color LABEL_TEXT = Color.WHITE;
    private static final float STROKE_WIDTH = 3f;

    public static void draw(Image img, DetectedObjects detections) {
        draw(img, detections, BOX_COLOR);
    }

    public static void draw(Image img, DetectedObjects detections, Color boxColor) {
        Color labelBg = new Color(
                Math.max(boxColor.getRed() - 55, 0),
                Math.max(boxColor.getGreen() - 55, 0),
                Math.max(boxColor.getBlue() - 55, 0),
                180);

        BufferedImage buffered = (BufferedImage) img.getWrappedImage();
        int w = buffered.getWidth();
        int h = buffered.getHeight();

        Graphics2D g = buffered.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(STROKE_WIDTH));

        Font font = new Font("SansSerif", Font.BOLD, 14);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        List<DetectedObjects.DetectedObject> items = detections.items();
        for (DetectedObjects.DetectedObject item : items) {
            Rectangle rect = item.getBoundingBox().getBounds();
            int x = (int) (rect.getX() * w);
            int y = (int) (rect.getY() * h);
            int bw = (int) (rect.getWidth() * w);
            int bh = (int) (rect.getHeight() * h);

            // Bounding box
            g.setColor(boxColor);
            g.drawRect(x, y, bw, bh);

            // Label chip: "bat - 87.5 %"
            String confidence = new DecimalFormat("#.##").format(item.getProbability() * 100) + " %";
            String label = item.getClassName() + " - " + confidence;
            int textW = fm.stringWidth(label);
            int textH = fm.getHeight();
            int labelY = y > textH + 4 ? y - textH - 2 : y + bh + textH + 2;

            g.setColor(labelBg);
            g.fillRect(x, labelY - textH + fm.getDescent(), textW + 6, textH + 2);

            g.setColor(LABEL_TEXT);
            g.drawString(label, x + 3, labelY);
        }
        g.dispose();
    }
}
