package org.theitdojo.predictive.clustering;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Renders a 2D scatter plot (PCA projection) with points colored by cluster
 * and centroid markers with semantic labels.
 */
public final class ClusterPlotRenderer {

    private ClusterPlotRenderer() {}

    public static Path renderPng(
            List<ClusterPoint2D> points,
            Map<Integer, ClusterPoint2D> centroids,
            Map<Integer, String> labels,
            int k,
            Path outputPath
    ) throws Exception {

        int width = 1200;
        int height = 800;
        int pad = 60;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Axes
        g.setColor(new Color(220, 220, 220));
        g.drawLine(pad, height - pad, width - pad, height - pad);
        g.drawLine(pad, pad, pad, height - pad);

        // Bounds (include centroids)
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (var p : points) {
            minX = Math.min(minX, p.x());
            maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = Math.max(maxY, p.y());
        }
        for (var c : centroids.values()) {
            minX = Math.min(minX, c.x());
            maxX = Math.max(maxX, c.x());
            minY = Math.min(minY, c.y());
            maxY = Math.max(maxY, c.y());
        }
        if (minX == maxX) { minX -= 1; maxX += 1; }
        if (minY == maxY) { minY -= 1; maxY += 1; }

        // Palette
        Color[] palette = new Color[] {
                new Color(31, 119, 180),
                new Color(255, 127, 14),
                new Color(44, 160, 44),
                new Color(214, 39, 40),
                new Color(148, 103, 189),
                new Color(140, 86, 75),
                new Color(227, 119, 194),
                new Color(127, 127, 127)
        };

        // Points
        int r = 5;
        for (var p : points) {
            int cx = scale(p.x(), minX, maxX, pad, width - pad);
            int cy = scale(p.y(), minY, maxY, height - pad, pad);
            Color c = palette[Math.floorMod(p.clusterId(), palette.length)];
            g.setColor(c);
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        }

        // Centroids (star markers)
        g.setStroke(new BasicStroke(2f));
        for (var e : centroids.entrySet()) {
            int cid = e.getKey();
            ClusterPoint2D c = e.getValue();

            int cx = scale(c.x(), minX, maxX, pad, width - pad);
            int cy = scale(c.y(), minY, maxY, height - pad, pad);

            drawStar(g, cx, cy, 12, 6, Color.BLACK, Color.WHITE);

            String label = labels.getOrDefault(cid, "Cluster " + cid);
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString(label, cx + 10, cy - 10);
        }

        // Title
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Customer Segments (K-Means + PCA)", pad, 30);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("k=" + k + " | points=" + points.size() + " | features=" + CustomerVectorizer.dimension(), pad, 50);
        g.drawString("PCA Component 1", width / 2 - 60, height - 20);

        // Y label
        AffineTransform orig = g.getTransform();
        g.rotate(-Math.PI / 2);
        g.drawString("PCA Component 2", -height / 2 - 60, 20);
        g.setTransform(orig);

        g.dispose();

        File out = outputPath.toFile();
        if (out.getParentFile() != null) out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        return outputPath;
    }

    private static void drawStar(Graphics2D g, int x, int y, int rOuter, int rInner, Color stroke, Color fill) {
        Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 5 * i - Math.PI / 2;
            int r = (i % 2 == 0) ? rOuter : rInner;
            star.addPoint(
                    x + (int) (Math.cos(angle) * r),
                    y + (int) (Math.sin(angle) * r)
            );
        }
        g.setColor(fill);
        g.fillPolygon(star);
        g.setColor(stroke);
        g.drawPolygon(star);
    }

    private static int scale(double v, double min, double max, int outMin, int outMax) {
        double t = (v - min) / (max - min);
        return (int) Math.round(outMin + t * (outMax - outMin));
    }
}
