package service;

import util.JsonUtil;
import util.ProcessRunnerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static util.JsonUtil.escapeJson;
import static util.JsonUtil.isBlank;

//service to generate audio from the images descriptions using TTS from openai
public class OpenAITTSService {


    private static final String AUDIO_SPEECH_URL = "https://api.openai.com/v1/audio/speech";
    private static final String DEFAULT_TTS_MODEL = "gpt-4o-mini-tts";
    private static final String DEFAULT_VOICE = "alloy";
    private static final String HTTP_STATUS_MARKER = "__HTTP_STATUS__:";

    private final String apiKey;
    private final String ttsModel;
    private final String voice;
    private final double speed;
    private final File workDir;
    private final int maxRetries;
    private final long initialBackoffMs;


    public OpenAITTSService(String apiKey, String ttsModel, String voice, double speed, File workDir, int maxRetries, long initialBackoffMs) {
        this.apiKey = apiKey;
        this.ttsModel = isBlank(ttsModel) ? DEFAULT_TTS_MODEL : ttsModel;
        this.voice = isBlank(voice) ? DEFAULT_VOICE : voice;
        this.speed = clampSpeed(speed);
        this.workDir = workDir;
        this.maxRetries = Math.max(0, maxRetries);
        this.initialBackoffMs = Math.max(300L, initialBackoffMs);
    }


    //converts text to speech and saves the result as mp3, so it can later be used in the ffmpeg service to join it with an image
    public File synthesize(String text, String fileName) throws IOException, InterruptedException {
        File outputFile = new File(workDir, fileName + ".mp3");
        postForAudio(buildTtsRequest(text), outputFile);
        if (!outputFile.isFile() || outputFile.length() == 0) {
            throw new RuntimeException("OpenAI TTS returned an empty audio payload.");
        }
        return outputFile;
    }

    private void postForAudio(String requestBody, File outputFile) throws IOException, InterruptedException {
        File requestFile = File.createTempFile("openai_tts_request_", ".json", workDir);
        File responseFile = File.createTempFile("openai_tts_response_", ".bin", workDir);
        Files.writeString(requestFile.toPath(), requestBody);

        List<String> command = Arrays.asList(
                "curl", "-sS",
                "-H", "Authorization: Bearer " + apiKey,
                "-H", "Content-Type: application/json",
                "-d", "@" + requestFile.getAbsolutePath(),
                "-o", responseFile.getAbsolutePath(),
                "-w", HTTP_STATUS_MARKER + "%{http_code}",
                AUDIO_SPEECH_URL
        );

        long backoffMs = initialBackoffMs;

        //same for the openai service, since i was hitting quota limits, I needed to retry the request with backoff, so i implemented a simple retry mechanism here as well, with exponential backoff and jitter to reduce the chance of collisions if multiple requests are being made at the same time
        try {
            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                ProcessRunnerUtil.ProcessResult result = ProcessRunnerUtil.run(command);
                int statusCode = parseStatusCode(result.getStdout());

                if (result.isSuccess() && statusCode >= 200 && statusCode < 300) {
                    Files.copy(responseFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return;
                }

                String body = Files.exists(responseFile.toPath())
                        ? Files.readString(responseFile.toPath(), StandardCharsets.UTF_8)
                        : "";

                if (JsonUtil.isRetryableStatus(statusCode) && attempt < maxRetries) {
                    JsonUtil.sleepWithBackoff(backoffMs);
                    backoffMs = Math.min(backoffMs * 2L, 8000L);
                    continue;
                }

                String detail = JsonUtil.extractErrorMessage(body);
                if (isBlank(detail)) {
                    detail = isBlank(result.getStderr()) ? body : result.getStderr();
                }
                throw new RuntimeException("OpenAI TTS call failed (HTTP " + statusCode + "): " + detail);
            }
        } finally {
            requestFile.delete();
            responseFile.delete();
        }

        throw new RuntimeException("OpenAI TTS call failed after retries.");
    }


    //helper to build the prompt
    private String buildTtsRequest(String text) {
        return "{"
                + "\"model\":\"" + escapeJson(ttsModel) + "\","
                + "\"voice\":\"" + escapeJson(voice) + "\","
                + "\"input\":\"" + escapeJson(text) + "\","
                + "\"speed\":" + speed + ","
                + "\"response_format\":\"mp3\""
                + "}";
    }

    //prevent invalid speed values that could cause the API to reject the request or produce unusable audio
    private double clampSpeed(double value) {
        if (value < 0.25) return 0.25;
        if (value > 2.0)  return 2.0;
        return value;
    }

    private int parseStatusCode(String rawOutput) {
        int marker = rawOutput == null ? -1 : rawOutput.lastIndexOf(HTTP_STATUS_MARKER);
        if (marker < 0) return 0;
        String statusPart = rawOutput.substring(marker + HTTP_STATUS_MARKER.length()).trim();
        try {
            return Integer.parseInt(statusPart);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
