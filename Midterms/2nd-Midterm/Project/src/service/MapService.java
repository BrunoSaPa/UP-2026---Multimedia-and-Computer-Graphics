package service;

import model.GPSLocation;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

//generate a map using openai api
public class MapService {

    private static final int CANVAS_WIDTH  = 1080;
    private static final int CANVAS_HEIGHT = 1920;
    private static final int MAP_X      = 60;
    private static final int MAP_Y      = 170;
    private static final int MAP_WIDTH  = 960;
    private static final int MAP_HEIGHT = 1340;

    private final OpenAIService openAIService;
    private final File workDir;

    public MapService(OpenAIService openAIService, File workDir) {
        this.openAIService = openAIService;
        this.workDir = workDir;
    }

    public File generateMapImage(List<GPSLocation> locations, String phrase)
            throws IOException, InterruptedException {
        if (locations == null || locations.size() < 2) {
            throw new IllegalArgumentException("Map image requires at least two GPS locations.");
        }

        //ask OpenAI to generate the map illustration
        String prompt = buildMapPrompt(locations, phrase);
        File aiImageFile = openAIService.generateImage(prompt, "map_ai");

        BufferedImage aiImage = ImageIO.read(aiImageFile);
        if (aiImage == null) {
            throw new IOException("Failed to read AI-generated map image: " + aiImageFile.getName());
        }

        //compose final canvas
        BufferedImage canvas = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            drawBackground(g);
            drawFrameMat(g);
            g.drawImage(aiImage, MAP_X, MAP_Y, MAP_WIDTH, MAP_HEIGHT, null);
            drawFrameBorder(g);
            drawLocationLabels(g, locations);
            drawPhraseCard(g, phrase);
        } finally {
            g.dispose();
        }

        File outputFile = new File(workDir, "map_final.png");
        ImageIO.write(canvas, "png", outputFile);
        return outputFile;
    }

    // Constructs a detailed prompt for the AI image generation, describing the desired map style and content based on the provided GPS locations and optional phrase.
    private String buildMapPrompt(List<GPSLocation> locations, String phrase) {
        GPSLocation start = locations.get(0);
        GPSLocation end = locations.get(locations.size() - 1);

        String startName = labelFor(start);
        String endName = labelFor(end);

        String routeStops = locations.stream()
                .map(this::labelFor)
                .collect(Collectors.joining(" → "));

        String moodLine = (phrase != null && !phrase.isBlank())
                ? " Mood/theme of the journey: \"" + phrase.trim() + "\"."
                : "";

        return "A beautiful hand-illustrated travel map in vertical portrait orientation (9:16 ratio). "
                + "Show a clear journey route from " + startName + " to " + endName + ". "
                + "Route stops in order: " + routeStops + ". "
                + "Style: vintage cartographic illustration, warm earthy parchment tones, "
                + "the route drawn as a winding path with small pin markers at each stop, "
                + "subtle terrain and landscape details (mountains, water bodies, roads), "
                + "a compass rose in one corner, decorative map border, soft warm lighting. "
                + "Cinematic, travel-poster quality." + moodLine
                + " Do not include any text or labels.";
    }



    //helpers to draw the image
    private void drawBackground(Graphics2D g) {
        g.setColor(new Color(236, 238, 241));
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }


    //map sits on top of this
    private void drawFrameMat(Graphics2D g) {
        g.setColor(new Color(250, 250, 250));
        g.fill(new RoundRectangle2D.Double(MAP_X - 8, MAP_Y - 8, MAP_WIDTH + 16, MAP_HEIGHT + 16, 24, 24));
    }


    private void drawFrameBorder(Graphics2D g) {
        g.setColor(new Color(95, 99, 104, 180));
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(MAP_X - 8, MAP_Y - 8, MAP_WIDTH + 16, MAP_HEIGHT + 16, 24, 24));
    }

    private void drawLocationLabels(Graphics2D g, List<GPSLocation> locations) {
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(new Color(33, 33, 33, 230));
        g.drawString("Start: " + labelFor(locations.get(0)), 80, 80);
        g.drawString("End: "   + labelFor(locations.get(locations.size() - 1)), 80, 122);
    }

    private void drawPhraseCard(Graphics2D g, String phrase) {
        String text = (phrase == null || phrase.isBlank()) ? "Every journey tells a story." : phrase.trim();

        int cardX = 90;
        int cardY = CANVAS_HEIGHT - 330;
        int cardWidth = CANVAS_WIDTH - 180;
        int cardHeight = 230;

        g.setColor(new Color(0, 0, 0, 115));
        g.fill(new RoundRectangle2D.Double(cardX, cardY, cardWidth, cardHeight, 48, 48));

        g.setColor(new Color(255, 255, 255, 220));
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("Journey Reflection", cardX + 40, cardY + 52);

        g.setFont(new Font("Serif", Font.BOLD, 40));
        drawCenteredWrappedText(g, text, cardX + 50, cardY + 95, cardWidth - 100);
    }

    private void drawCenteredWrappedText(Graphics2D g, String text, int x, int y, int maxWidth) {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int baseline = y;
        int lineHeight = 50;

        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (g.getFontMetrics().stringWidth(candidate) > maxWidth && !line.isEmpty()) {
                drawCenteredLine(g, line.toString(), x, baseline, maxWidth);
                line = new StringBuilder(word);
                baseline += lineHeight;
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            drawCenteredLine(g, line.toString(), x, baseline, maxWidth);
        }
    }

    private void drawCenteredLine(Graphics2D g, String text, int x, int baseline, int maxWidth) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        int drawX     = x + Math.max(0, (maxWidth - textWidth) / 2);
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(text, drawX + 2, baseline + 2);
        g.setColor(Color.WHITE);
        g.drawString(text, drawX, baseline);
    }


    //utility
    private String labelFor(GPSLocation location) {
        if (location.getPlaceName() != null && !location.getPlaceName().isBlank()) {
            return location.getPlaceName();
        }
        return location.toCoordinateString();
    }
}
