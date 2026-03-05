package org.theitdojo.predictive.tensorflow.renderer;

import org.theitdojo.predictive.tensorflow.detector.EfficientDetDetector.Detection;

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

    /**
     * Draws bounding boxes and labels onto the given image (mutates in place).
     */
    public static void draw(BufferedImage image, List<Detection> detections) {
        int w = image.getWidth();
        int h = image.getHeight();

        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(STROKE_WIDTH));

        Font font = new Font("SansSerif", Font.BOLD, 14);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        DecimalFormat pct = new DecimalFormat("#.##");

        for (Detection det : detections) {
            int x = (int) (det.xmin() * w);
            int y = (int) (det.ymin() * h);
            int bw = (int) ((det.xmax() - det.xmin()) * w);
            int bh = (int) ((det.ymax() - det.ymin()) * h);

            // Bounding box
            g.setColor(BOX_COLOR);
            g.drawRect(x, y, bw, bh);

            // Label chip
            String confidence = pct.format(det.score() * 100) + " %";
            String displayName = det.className();
            String label = displayName + " - " + confidence;
            int textW = fm.stringWidth(label);
            int textH = fm.getHeight();
            int labelY = y > textH + 4 ? y - textH - 2 : y + bh + textH + 2;

            g.setColor(LABEL_BG);
            g.fillRect(x, labelY - textH + fm.getDescent(), textW + 6, textH + 2);

            g.setColor(LABEL_TEXT);
            g.drawString(label, x + 3, labelY);
        }
        g.dispose();
    }
}
