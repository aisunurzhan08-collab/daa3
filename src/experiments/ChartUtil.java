package experiments;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
public class ChartUtil {
    private static final Color[] COLORS = {
        new Color(0x1f77b4), new Color(0xd62728), new Color(0x2ca02c), new Color(0xff7f0e), new Color(0x9467bd)
    };

    public static void lineChart(String title, String xLabel, String yLabel, double[] x,
                                 Map<String, double[]> series, String fileName) throws IOException {
        System.setProperty("java.awt.headless", "true");
        int w = 900, h = 560;
        int left = 90, right = 40, top = 60, bottom = 80;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        double minY = Double.MAX_VALUE, maxY = 0;
        for (double[] ys : series.values()) {
            for (double y : ys) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        double lx0 = Math.floor(Math.log10(x[0])), lx1 = Math.ceil(Math.log10(x[x.length - 1]));
        double ly0 = Math.floor(Math.log10(minY)), ly1 = Math.ceil(Math.log10(maxY));
        if (ly1 == ly0) {
            ly1++;
        }
        int pw = w - left - right, ph = h - top - bottom;

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int p = (int) lx0; p <= (int) lx1; p++) {
            int px = left + (int) ((p - lx0) / (lx1 - lx0) * pw);
            g.setColor(new Color(225, 225, 225));
            g.drawLine(px, top, px, top + ph);
            g.setColor(Color.BLACK);
            String s = "10^" + p;
            g.drawString(s, px - g.getFontMetrics().stringWidth(s) / 2, top + ph + 20);
        }
        for (int p = (int) ly0; p <= (int) ly1; p++) {
            int py = top + ph - (int) ((p - ly0) / (ly1 - ly0) * ph);
            g.setColor(new Color(225, 225, 225));
            g.drawLine(left, py, left + pw, py);
            g.setColor(Color.BLACK);
            String s = "10^" + p;
            g.drawString(s, left - 8 - g.getFontMetrics().stringWidth(s), py + 4);
        }
        g.setColor(Color.BLACK);
        g.drawRect(left, top, pw, ph);

        int ci = 0;
        for (Map.Entry<String, double[]> en : series.entrySet()) {
            g.setColor(COLORS[ci % COLORS.length]);
            g.setStroke(new BasicStroke(2f));
            double[] ys = en.getValue();
            int prevX = 0, prevY = 0;
            for (int i = 0; i < x.length; i++) {
                int px = left + (int) ((Math.log10(x[i]) - lx0) / (lx1 - lx0) * pw);
                int py = top + ph - (int) ((Math.log10(ys[i]) - ly0) / (ly1 - ly0) * ph);
                if (i > 0) {
                    g.drawLine(prevX, prevY, px, py);
                }
                g.fillOval(px - 4, py - 4, 8, 8);
                prevX = px;
                prevY = py;
            }
            int ly = top + 20 + ci * 20;
            g.fillRect(left + 15, ly - 10, 14, 4);
            g.setColor(Color.BLACK);
            g.drawString(en.getKey(), left + 35, ly - 4);
            ci++;
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (w - fm.stringWidth(title)) / 2, 30);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fm = g.getFontMetrics();
        g.drawString(xLabel, left + (pw - fm.stringWidth(xLabel)) / 2, h - 25);
        g.rotate(-Math.PI / 2);
        g.drawString(yLabel, -(top + (ph + fm.stringWidth(yLabel)) / 2), 25);
        g.dispose();

        File f = new File(fileName);
        f.getParentFile().mkdirs();
        ImageIO.write(img, "png", f);
    }
}
