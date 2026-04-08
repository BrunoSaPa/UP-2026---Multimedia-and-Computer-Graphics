package service;

import model.GPSLocation;
import model.MediaContent;
import model.MediaType;
import util.JsonUtil;
import util.ProcessRunnerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


//service to interact with openai text, and image generation api
public class OpenAIService {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String IMAGE_GENERATIONS_URL = "https://api.openai.com/v1/images/generations";
    private static final String DEFAULT_TEXT_MODEL = "gpt-4.1-mini";
    private static final String DEFAULT_IMAGE_MODEL = "gpt-image-1";
    private static final String HTTP_STATUS_MARKER = "__HTTP_STATUS__:";

    private final String apiKey;
    private final File workDir;
    private final String textModel;
    private final String imageModel;
    private final int maxRetries;
    private final long initialBackoffMs;


    //Needed to add maxRetries since i was hitting limits of request per minute and it was causing the whole process to fail, now it will retry a few times with backoff before giving up
    public OpenAIService(String apiKey, File workDir,
                         String textModel, String imageModel,
                         int maxRetries, long initialBackoffMs) {
        this.apiKey           = apiKey;
        this.workDir          = workDir;
        this.textModel        = JsonUtil.isBlank(textModel)  ? DEFAULT_TEXT_MODEL  : textModel;
        this.imageModel       = JsonUtil.isBlank(imageModel) ? DEFAULT_IMAGE_MODEL : imageModel;
        this.maxRetries       = Math.max(0, maxRetries);
        this.initialBackoffMs = Math.max(300L, initialBackoffMs);
    }

    public String describeContent(MediaContent media) throws IOException, InterruptedException {
        File imageFile = media.getType() == MediaType.VIDEO ? extractVideoThumbnail(media.getFile()) : media.getFile();

        String prompt = "Describe this travel moment in 1 concise sentence. "
                + "Focus on the place, atmosphere, and visually important details. "
                + "Write natural narration-ready English.";

        String response = callChatCompletion(buildVisionRequest(prompt, encodeFileToBase64(imageFile), getMimeType(imageFile)));

        String text = extractMessageContent(response);
        if (JsonUtil.isBlank(text)) {
            throw new RuntimeException("Failed to extract description from OpenAI response");
        }
        return text.trim();
    }

    //since one of the requirements was to get recognizable places, I ask openai to resolve the gps coordinates to a human readable place name
    public String resolveLocationName(GPSLocation location) throws IOException, InterruptedException {
        String prompt = "You are given GPS coordinates: "
                + location.getLatitude() + ", " + location.getLongitude() + ". "
                + "Return a concise human-readable place name such as a city, neighborhood, or landmark. "
                + "Return only the place name, no explanation.";

        String response = callChatCompletion(buildTextRequest(prompt, 80));
        String text = extractMessageContent(response);
        return JsonUtil.isBlank(text) ? "Unknown Location" : text.trim();
    }

    public String extractJourneyMood(List<MediaContent> mediaList) throws IOException, InterruptedException {
        String summaries = mediaList.stream()
                .map(MediaContent::getAiDescription)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("\n- ", "- ", ""));

        if (summaries.isBlank()) return "calm, exploratory, cinematic";

        String prompt = "Analyze these travel moments and return only a comma-separated list "
                + "of 3 to 6 adjectives describing the overall mood.\n" + summaries;

        String response = callChatCompletion(buildTextRequest(prompt, 80));
        String mood = extractMessageContent(response);
        return JsonUtil.isBlank(mood) ? "calm, exploratory, cinematic" : mood.trim();
    }

    public String generateInspirationPhrase(List<GPSLocation> allLocations)
            throws IOException, InterruptedException {
        String locationsList = allLocations.stream()
                .map(loc -> !JsonUtil.isBlank(loc.getPlaceName()) ? loc.getPlaceName() : loc.toCoordinateString())
                .collect(Collectors.joining(", "));

        String prompt = "Based on a journey through these places: " + locationsList + ". "
                + "Write one short inspirational travel phrase with at most 15 words. "
                + "Return only the phrase.";

        String response = callChatCompletion(buildTextRequest(prompt, 60));
        String text = extractMessageContent(response);
        if (JsonUtil.isBlank(text)) {
            throw new RuntimeException("Failed to extract inspiration phrase from OpenAI response");
        }
        return text.trim();
    }

    //generates image from a prompt and saves it in the workdir
    public File generateImage(String prompt, String outputName)
            throws IOException, InterruptedException {
        String requestBody = "{"
                + "\"model\":\"" + JsonUtil.escapeJson(imageModel) + "\","
                + "\"prompt\":\"" + JsonUtil.escapeJson(prompt) + "\","
                + "\"size\":\"1024x1536\""
                + "}";
        String response = callImageGeneration(requestBody);
        byte[] imageBytes = extractImageBytes(response);
        if (imageBytes == null || imageBytes.length == 0) {
            throw new RuntimeException("Failed to extract image data from OpenAI response");
        }
        File outputFile = new File(workDir, outputName + "_" + System.currentTimeMillis() + ".png");
        Files.write(outputFile.toPath(), imageBytes);
        return outputFile;
    }

    public File generateEssenceImage(List<MediaContent> mediaList, String journeyMood)
            throws IOException, InterruptedException {
        String contentSummary = mediaList.stream()
                .map(MediaContent::getAiDescription)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("; "));

        if (contentSummary.isBlank()) {
            throw new RuntimeException("Cannot generate intro image without media descriptions.");
        }

        String prompt = "Create a vertical 9:16 cinematic travel poster that captures the essence of this journey. "
                + "Use ALL of these travel moments as inspiration: " + contentSummary + ". "
                + "Mood profile: " + (JsonUtil.isBlank(journeyMood) ? "calm, exploratory, cinematic" : journeyMood) + ". "
                + "This must be a unique composition (not a copy of a single photo frame). "
                + "Rich detail, cohesive composition, emotionally evocative, no text overlays.";

        String response = callImageGeneration(buildImageRequest(prompt));
        byte[] imageBytes = extractImageBytes(response);
        if (imageBytes == null || imageBytes.length == 0) {
            throw new RuntimeException("Failed to extract image data from OpenAI response");
        }

        File outputFile = new File(workDir, "essence_image_" + System.currentTimeMillis() + ".png");
        Files.write(outputFile.toPath(), imageBytes);
        return outputFile;
    }

    // ── HTTP helpers ────────────────────────────────────────────────────────

    private String callChatCompletion(String requestBody) throws IOException, InterruptedException {
        return postJson(CHAT_COMPLETIONS_URL, requestBody);
    }

    private String callImageGeneration(String requestBody) throws IOException, InterruptedException {
        return postJson(IMAGE_GENERATIONS_URL, requestBody);
    }

    //handles the actual call to the api, using the util to execute commands
    private String postJson(String url, String requestBody)
            throws IOException, InterruptedException {

        File requestFile = File.createTempFile("openai_request_", ".json", workDir);
        Files.writeString(requestFile.toPath(), requestBody);

        List<String> command = Arrays.asList(
                "curl", "-sS",
                "-H", "Authorization: Bearer " + apiKey,
                "-H", "Content-Type: application/json",
                "-d", "@" + requestFile.getAbsolutePath(),
                "-w", "\\n" + HTTP_STATUS_MARKER + "%{http_code}",
                url
        );

        long backoffMs = initialBackoffMs;

        try {
            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                ProcessRunnerUtil.ProcessResult result = ProcessRunnerUtil.run(command);
                ParsedHttpResponse parsed = parseHttpResponse(result.getStdout());

                if (result.isSuccess() && parsed.statusCode >= 200 && parsed.statusCode < 300) {
                    return parsed.body;
                }

                if (JsonUtil.isRetryableStatus(parsed.statusCode) && attempt < maxRetries) {
                    JsonUtil.sleepWithBackoff(backoffMs);
                    backoffMs = Math.min(backoffMs * 2L, 8000L);
                    continue;
                }

                String detail = JsonUtil.extractErrorMessage(parsed.body);
                if (JsonUtil.isBlank(detail)) {
                    detail = JsonUtil.isBlank(result.getStderr()) ? parsed.body : result.getStderr();
                }
                throw new RuntimeException("OpenAI API call failed (HTTP " + parsed.statusCode + "): " + detail);
            }
        } finally {
            requestFile.delete();
        }

        throw new RuntimeException("OpenAI API call failed after retries.");
    }

    // ── Response parsing ────────────────────────────────────────────────────

    private ParsedHttpResponse parseHttpResponse(String rawOutput) {
        int marker = rawOutput == null ? -1 : rawOutput.lastIndexOf(HTTP_STATUS_MARKER);
        if (marker < 0) return new ParsedHttpResponse(0, rawOutput == null ? "" : rawOutput);

        String body       = rawOutput.substring(0, marker).trim();
        String statusPart = rawOutput.substring(marker + HTTP_STATUS_MARKER.length()).trim();
        try {
            return new ParsedHttpResponse(Integer.parseInt(statusPart), body);
        } catch (NumberFormatException e) {
            return new ParsedHttpResponse(0, body);
        }
    }

    private record ParsedHttpResponse(int statusCode, String body) { }

    private String extractMessageContent(String json) {
        // Try  choices[].message.content
        int messageIdx = json.indexOf("\"message\"");
        if (messageIdx >= 0) {
            String content = JsonUtil.extractString(json.substring(messageIdx), "content");
            if (!JsonUtil.isBlank(content)) return content;
        }
        // Try  choices[].message.content (array form)
        int contentArr = json.indexOf("\"content\":[");
        if (contentArr >= 0) {
            int textIdx = json.indexOf("\"text\"", contentArr);
            if (textIdx >= 0) {
                String nested = JsonUtil.extractString(json.substring(textIdx), "text");
                if (!JsonUtil.isBlank(nested)) return nested;
            }
        }
        // Fallback
        return JsonUtil.extractString(json, "output_text");
    }

    private byte[] extractImageBytes(String body) {
        String b64 = JsonUtil.extractString(body, "b64_json");
        if (!JsonUtil.isBlank(b64)) return Base64.getDecoder().decode(b64);

        String url = JsonUtil.extractString(body, "url");
        if (!JsonUtil.isBlank(url)) {
            File tempImage = new File(workDir, "img_download_" + System.nanoTime() + ".png");
            try {
                ProcessRunnerUtil.runOrThrow(Arrays.asList(
                        "curl", "-sS", "-L", "-o", tempImage.getAbsolutePath(), url));
                if (tempImage.isFile() && tempImage.length() > 0) {
                    byte[] bytes = Files.readAllBytes(tempImage.toPath());
                    tempImage.delete();
                    return bytes;
                }
            } catch (Exception ignored) {
                tempImage.delete();
            }
        }
        return null;
    }

    // ── Request builders ────────────────────────────────────────────────────

    private String buildTextRequest(String prompt, int maxTokens) {
        return "{"
                + "\"model\":\"" + JsonUtil.escapeJson(textModel) + "\","
                + "\"messages\":[{\"role\":\"user\",\"content\":\"" + JsonUtil.escapeJson(prompt) + "\"}],"
                + "\"temperature\":0.4,"
                + "\"max_tokens\":" + maxTokens
                + "}";
    }

    private String buildVisionRequest(String prompt, String base64Image, String mimeType) {
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;
        return "{"
                + "\"model\":\"" + JsonUtil.escapeJson(textModel) + "\","
                + "\"messages\":[{"
                + "\"role\":\"user\","
                + "\"content\":["
                + "{\"type\":\"text\",\"text\":\"" + JsonUtil.escapeJson(prompt) + "\"},"
                + "{\"type\":\"image_url\",\"image_url\":{\"url\":\"" + dataUrl + "\"}}"
                + "]}],"
                + "\"temperature\":0.5,"
                + "\"max_tokens\":220"
                + "}";
    }

    private String buildImageRequest(String prompt) {
        return "{"
                + "\"model\":\"" + JsonUtil.escapeJson(imageModel) + "\","
                + "\"prompt\":\"" + JsonUtil.escapeJson(prompt) + "\","
                + "\"size\":\"auto\""
                + "}";
    }

    // ── Media helpers ───────────────────────────────────────────────────────

    //since i need to send an image to analyze the content of a video, i extract a thumbnail using ffmpeg
    private File extractVideoThumbnail(File videoFile) throws IOException, InterruptedException {
        File thumbnail = new File(workDir, "thumb_" + videoFile.getName() + ".jpg");
        ProcessRunnerUtil.runOrThrow(List.of(
                "ffmpeg", "-y", "-i", videoFile.getAbsolutePath(),
                "-vframes", "1", "-q:v", "2",
                thumbnail.getAbsolutePath()));
        return thumbnail;
    }

    private String encodeFileToBase64(File file) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    }

    private String getMimeType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".gif"))  return "image/gif";
        if (name.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}

