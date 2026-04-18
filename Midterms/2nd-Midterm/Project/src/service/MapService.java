package service;

import model.GPSLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import util.ProcessRunnerUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//generates map with openstreetmap with the first and last locations visited
public class MapService {

    private static final int CANVAS_WIDTH = 1080;
    private static final int CANVAS_HEIGHT = 1920;
    private static final int TILE_SIZE = 256;
    private static final String TILE_URL = "https://tile.openstreetmap.org/%d/%d/%d.png";

    private final File workDir;

    public MapService(File workDir) {
        this.workDir = workDir;
    }

    public File generateMapImage(List<GPSLocation> locations, String phrase)
            throws IOException, InterruptedException {
        if (locations == null || locations.size() < 2)
            throw new IllegalArgumentException("Need at least two GPS locations for the map.");

        GPSLocation first = locations.get(0);
        GPSLocation last = locations.get(locations.size() - 1);

        int zoom = bestZoom(first, last);

        double centerLat = (first.getLatitude() + last.getLatitude()) / 2.0;
        double centerLon = (first.getLongitude() + last.getLongitude()) / 2.0;

        int tilesX = (int) Math.ceil((double) CANVAS_WIDTH / TILE_SIZE) + 2;
        int tilesY = (int) Math.ceil((double) CANVAS_HEIGHT / TILE_SIZE) + 2;

        double centerTileX = lonToTileX(centerLon, zoom);
        double centerTileY = latToTileY(centerLat, zoom);

        int startTileX = (int) Math.floor(centerTileX) - tilesX / 2;
        int startTileY = (int) Math.floor(centerTileY) - tilesY / 2;

        double offsetX = (CANVAS_WIDTH / 2.0) - (centerTileX - startTileX) * TILE_SIZE;
        double offsetY = (CANVAS_HEIGHT / 2.0) - (centerTileY - startTileY) * TILE_SIZE;

        BufferedImage canvas = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // fill background grey in case tiles fail
        g.setColor(new Color(200, 200, 200));
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // draw map tiles
        for (int tx = 0; tx < tilesX; tx++) {
            for (int ty = 0; ty < tilesY; ty++) {
                int tileX = startTileX + tx;
                int tileY = startTileY + ty;
                int px = (int) Math.round(offsetX + tx * TILE_SIZE);
                int py = (int) Math.round(offsetY + ty * TILE_SIZE);
                try {
                    BufferedImage tile = downloadTile(zoom, tileX, tileY);
                    if (tile != null) {
                        g.drawImage(tile, px, py, TILE_SIZE, TILE_SIZE, null);
                    }
                } catch (IOException | InterruptedException e) {
                    System.err.println("Tile download failed: z=" + zoom + " x=" + tileX + " y=" + tileY);
                }
            }
        }

        // dashed line between pins
        int[] pxFirst = gpsToPixel(first, zoom, startTileX, startTileY, offsetX, offsetY);
        int[] pxLast = gpsToPixel(last, zoom, startTileX, startTileY, offsetX, offsetY);

        g.setColor(new Color(220, 50, 50, 200));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{14, 10}, 0));
        g.drawLine(pxFirst[0], pxFirst[1], pxLast[0], pxLast[1]);

        // pins
        drawPin(g, pxFirst[0], pxFirst[1], new Color(0, 150, 0), "START");
        drawPin(g, pxLast[0], pxLast[1], new Color(200, 30, 30), "END");

        // phrase bar at bottom with word wrap
        if (phrase != null && !phrase.isBlank()) {
            drawPhraseBar(g, phrase);
        }

        g.dispose();

        File out = new File(workDir, "map_final.png");
        ImageIO.write(canvas, "png", out);
        return out;
    }



    private double lonToTileX(double lon, int zoom) {
        return (lon + 180.0) / 360.0 * (1 << zoom);
    }

    private double latToTileY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return (1 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * (1 << zoom);
    }

    private int[] gpsToPixel(GPSLocation loc, int zoom, int startTileX, int startTileY, double offsetX, double offsetY) {
        double tx = lonToTileX(loc.getLongitude(), zoom) - startTileX;
        double ty = latToTileY(loc.getLatitude(), zoom) - startTileY;
        int px = (int) Math.round(offsetX + tx * TILE_SIZE);
        int py = (int) Math.round(offsetY + ty * TILE_SIZE);
        return new int[]{px, py};
    }

    private int bestZoom(GPSLocation a, GPSLocation b) {
        double latDiff = Math.abs(a.getLatitude() - b.getLatitude());
        double lonDiff = Math.abs(a.getLongitude() - b.getLongitude());
        double maxSpan = Math.max(latDiff, lonDiff);
        for (int z = 15; z >= 1; z--) {
            double degreesPerTile = 360.0 / (1 << z);
            double tilesNeeded = maxSpan / degreesPerTile;
            double pixelsNeeded = tilesNeeded * TILE_SIZE;
            // both pins must fit within 60% of canvas width
            if (pixelsNeeded < CANVAS_WIDTH * 0.5) {
                return z;
            }
        }
        return 4;
    }

    private BufferedImage downloadTile(int zoom, int x, int y) throws IOException, InterruptedException {
        // wrap tile x to valid range
        int maxTile = 1 << zoom;
        x = ((x % maxTile) + maxTile) % maxTile;
        if (y < 0 || y >= maxTile) return null;

        String url = String.format(TILE_URL, zoom, x, y);
        File tileFile = new File(workDir, "tile_" + zoom + "_" + x + "_" + y + ".png");

        ProcessRunnerUtil.ProcessResult result = ProcessRunnerUtil.run(List.of(
                "curl", "-s", "-o", tileFile.getAbsolutePath(),
                "-H", "User-Agent: TravelVideoBuilder/1.0",
                url
        ));

        if (!result.isSuccess() || !tileFile.exists() || tileFile.length() == 0) {
            tileFile.delete();
            return null;
        }

        BufferedImage img = ImageIO.read(tileFile);
        tileFile.delete();
        return img;
    }


    //draw pin

    private void drawPin(Graphics2D g, int x, int y, Color color, String label) {
        int pinH = 40, pinW = 28;
        int[] xPoly = {x, x - pinW / 2, x + pinW / 2};
        int[] yPoly = {y, y - pinH, y - pinH};
        g.setColor(color);
        g.fillPolygon(xPoly, yPoly, 3);
        g.fill(new Ellipse2D.Double(x - pinW / 2.0, y - pinH - pinW / 2.0, pinW, pinW));

        g.setColor(Color.WHITE);
        int dotR = 6;
        g.fill(new Ellipse2D.Double(x - dotR, y - pinH - dotR, dotR * 2, dotR * 2));

        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        int tw = g.getFontMetrics().stringWidth(label);
        int lx = x - tw / 2;
        int ly = y - pinH - pinW / 2 - 10;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(lx - 8, ly - 20, tw + 16, 28, 10, 10);
        g.setColor(Color.WHITE);
        g.drawString(label, lx, ly);
    }

    private void drawPhraseBar(Graphics2D g, String phrase) {
        Font font = new Font("SansSerif", Font.BOLD, 26);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int padding = 30;
        int maxTextWidth = CANVAS_WIDTH - padding * 2;
        List<String> lines = wrapText(phrase, fm, maxTextWidth);

        int lineHeight = fm.getHeight() + 4;
        int barH = padding * 2 + lines.size() * lineHeight;
        int barY = CANVAS_HEIGHT - barH;

        // semi transparent background
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, barY, CANVAS_WIDTH, barH);

        // draw each line of texrt centered
        g.setColor(Color.WHITE);
        int textY = barY + padding + fm.getAscent();
        for (String line : lines) {
            int tw = fm.stringWidth(line);
            int tx = (CANVAS_WIDTH - tw) / 2;
            g.drawString(line, tx, textY);
            textY += lineHeight;
        }
    }

    //helper function to account for how many lines of text ill have, because the text was overflowing if i didnt take this into account
    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (fm.stringWidth(candidate) > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }
}
