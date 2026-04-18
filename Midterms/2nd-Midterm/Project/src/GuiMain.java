import pipeline.VideoProjectBuilder;
import service.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;


public class GuiMain {

    // media file filter (same extensions as VideoProjectBuilder)
    private static final String[] MEDIA_EXTENSIONS = {".jpg", ".jpeg", ".png", ".heic", ".mp4", ".mov", ".avi", ".mkv"};

    private static final int THUMB_SIZE = 120;

    private JFrame frame;
    private JLabel folderLabel;
    private JPanel thumbnailPanel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton buildButton;
    private JButton folderButton;
    private File selectedFolder;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GuiMain().createAndShowGui());
    }

    private void createAndShowGui() {
        frame = new JFrame("Travel Video Builder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 520);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(8, 8));

        //choose folder
        JPanel topPanel = new JPanel(new BorderLayout(6, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        folderButton = new JButton("Select Folder…");
        folderLabel = new JLabel("No folder selected");
        folderLabel.setForeground(Color.GRAY);
        topPanel.add(folderButton, BorderLayout.WEST);
        topPanel.add(folderLabel, BorderLayout.CENTER);
        frame.add(topPanel, BorderLayout.NORTH);

        // thumbail in the center
        thumbnailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JScrollPane scroll = new JScrollPane(thumbnailPanel);
        scroll.setBorder(BorderFactory.createTitledBorder("Media Preview"));
        scroll.setPreferredSize(new Dimension(700, 300));
        frame.add(scroll, BorderLayout.CENTER);

        // bottom progress and build video button
        JPanel bottomPanel = new JPanel(new BorderLayout(6, 4));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        statusLabel = new JLabel("Idle");
        progressBar = new JProgressBar(0, 10);
        progressBar.setStringPainted(true);
        progressBar.setString("0 / 10");

        buildButton = new JButton("Build Video");
        buildButton.setEnabled(false);

        JPanel progressRow = new JPanel(new BorderLayout(6, 0));
        progressRow.add(progressBar, BorderLayout.CENTER);
        progressRow.add(buildButton, BorderLayout.EAST);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(progressRow, BorderLayout.SOUTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        //actions
        folderButton.addActionListener(e -> chooseFolder());
        buildButton.addActionListener(e -> startBuild());

        frame.setVisible(true);
    }

    //folder selection

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select folder with photos / videos");
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            selectedFolder = chooser.getSelectedFile();
            folderLabel.setText(selectedFolder.getAbsolutePath());
            folderLabel.setForeground(Color.BLACK);
            loadThumbnails();
            buildButton.setEnabled(true);
        }
    }

    //thubnail loading

    private void loadThumbnails() {
        thumbnailPanel.removeAll();

        File[] files = selectedFolder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            for (String ext : MEDIA_EXTENSIONS) {
                if (lower.endsWith(ext)) return true;
            }
            return false;
        });

        if (files == null || files.length == 0) {
            thumbnailPanel.add(new JLabel("No media files found in this folder."));
            thumbnailPanel.revalidate();
            thumbnailPanel.repaint();
            buildButton.setEnabled(false);
            return;
        }

        for (File f : files) {
            ImageIcon icon = makeThumbnail(f);
            JLabel lbl = new JLabel(f.getName(), icon, JLabel.CENTER);
            lbl.setVerticalTextPosition(JLabel.BOTTOM);
            lbl.setHorizontalTextPosition(JLabel.CENTER);
            lbl.setPreferredSize(new Dimension(THUMB_SIZE + 20, THUMB_SIZE + 30));
            lbl.setFont(lbl.getFont().deriveFont(9f));
            thumbnailPanel.add(lbl);
        }
        thumbnailPanel.revalidate();
        thumbnailPanel.repaint();
    }

    private ImageIcon makeThumbnail(File file) {
        String lower = file.getName().toLowerCase();
        boolean isVideo = lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi") || lower.endsWith(".mkv");

        BufferedImage img = null;
        if (isVideo) {
            img = extractVideoFrame(file);
        } else {
            try { img = ImageIO.read(file); } catch (Exception ignored) {}
        }

        if (img == null) {
            //fallback
            img = new BufferedImage(THUMB_SIZE, THUMB_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(isVideo ? new Color(60, 60, 80) : Color.LIGHT_GRAY);
            g.fillRect(0, 0, THUMB_SIZE, THUMB_SIZE);
            g.setColor(Color.WHITE);
            g.drawString(isVideo ? "VIDEO" : "IMG", 30, 65);
            g.dispose();
        }

        // scale keeping aspect ratio
        int w = img.getWidth(), h = img.getHeight();
        double scale = Math.min((double) THUMB_SIZE / w, (double) THUMB_SIZE / h);
        int tw = Math.max(1, (int) (w * scale));
        int th = Math.max(1, (int) (h * scale));
        Image scaled = img.getScaledInstance(tw, th, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    //use ffmpeg to get first frame for videos
    private BufferedImage extractVideoFrame(File video) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", video.getAbsolutePath(),
                    "-frames:v", "1", "-f", "image2pipe", "-vcodec", "png", "-"
            );
            pb.redirectErrorStream(false);
            Process proc = pb.start();
            // read stdout (the png bytes)
            InputStream is = proc.getInputStream();
            BufferedImage img = ImageIO.read(is);
            proc.waitFor();
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    //build pipeline

    private void startBuild() {
        buildButton.setEnabled(false);
        folderButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("0 / 10");
        statusLabel.setText("Starting…");

        //steps to show in the progress bar
        SwingWorker<File, String> worker = new SwingWorker<>() {
            @Override
            protected File doInBackground() throws Exception {
                // read env / config (same logic as Main.java)
                String openAiKey = System.getenv("OpenAIToken");
                if (openAiKey == null || openAiKey.isBlank()) {
                    throw new RuntimeException("Environment variable OpenAIToken is not set.");
                }
                String textModel  = envOr("OPENAI_TEXT_MODEL",  "gpt-4.1-mini");
                String imageModel = envOr("OPENAI_IMAGE_MODEL", "gpt-image-1");
                String ttsModel   = envOr("OPENAI_TTS_MODEL",   "gpt-4o-mini-tts");
                String ttsVoice   = envOr("OPENAI_TTS_VOICE",   "alloy");
                double ttsSpeed   = doubleOr("OPENAI_TTS_SPEED", 1.1);
                int    retries    = intOr("OPENAI_MAX_RETRIES",  3);
                long   backoff    = intOr("OPENAI_RETRY_BACKOFF_MS", 1500);

                File workDir = new File(selectedFolder, ".travel_video_work");
                if (!workDir.exists() && !workDir.mkdirs()) {
                    throw new RuntimeException("Could not create work directory");
                }

                ExifToolService  exif    = new ExifToolService();
                OpenAIService    ai      = new OpenAIService(openAiKey, workDir, textModel, imageModel, retries, backoff);
                OpenAITTSService tts     = new OpenAITTSService(openAiKey, ttsModel, ttsVoice, ttsSpeed, workDir, retries, backoff);
                MapService       map     = new MapService(workDir);
                FFmpegService    ffmpeg  = new FFmpegService(workDir);

                VideoProjectBuilder builder = new VideoProjectBuilder(exif, ai, tts, map, ffmpeg, workDir);
                builder.setProgressListener((step, total, msg) ->
                        publish(step + "|" + total + "|" + msg)
                );

                File outputFile = new File(selectedFolder, "travel_video.mp4");
                return builder.build(selectedFolder, outputFile);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                String last = chunks.get(chunks.size() - 1);
                String[] parts = last.split("\\|", 3);
                int step  = Integer.parseInt(parts[0]);
                int total = Integer.parseInt(parts[1]);
                String msg = parts[2];
                progressBar.setValue(step);
                progressBar.setString(step + " / " + total);
                statusLabel.setText(msg);
            }

            @Override
            protected void done() {
                buildButton.setEnabled(true);
                folderButton.setEnabled(true);
                try {
                    File result = get();
                    progressBar.setValue(10);
                    progressBar.setString("finish");
                    statusLabel.setText("Video saved: " + result.getAbsolutePath());
                    JOptionPane.showMessageDialog(frame, "Video created successfully!\n" + result.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    statusLabel.setText("Error");
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }


    private static String envOr(String name, String def) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? def : v;
    }
    private static int intOr(String name, int def) {
        try { return Integer.parseInt(System.getenv(name)); } catch (Exception e) { return def; }
    }
    private static double doubleOr(String name, double def) {
        try { return Double.parseDouble(System.getenv(name)); } catch (Exception e) { return def; }
    }
}
