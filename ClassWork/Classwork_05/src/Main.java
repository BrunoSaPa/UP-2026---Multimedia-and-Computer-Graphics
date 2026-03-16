import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    private static final String INPUT_FILE_PATH = "./in.txt";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);


        //read env var
        String apiKey = System.getenv("OpenAIToken");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("env var not set");
            System.exit(1);
        }

        String inputPath = INPUT_FILE_PATH;

        File inputFile = new File(inputPath);
        if (!inputFile.exists() || !inputFile.isFile() || !inputPath.endsWith(".txt")) {
            System.err.println("not found " + inputPath);
            System.exit(1);
        }

        String fileContent = Files.readString(inputFile.toPath());
        if (fileContent.isBlank()) {
            System.err.println("empty file");
            System.exit(1);
        }


        System.out.print("Enter the target language: ");
        String targetLanguage = scanner.nextLine().trim();
        if (targetLanguage.isBlank()) {
            System.err.println("target cannot be empty");
            System.exit(1);
        }


        String escapedContent = escapeJson(fileContent);
        String jsonPayload = "{"
                + "\"model\":\"gpt-4o-mini\","
                + "\"messages\":["
                + "  {\"role\":\"system\","
                + "   \"content\":\"You are a professional translator. "
                + "Translate the user's text to " + escapeJson(targetLanguage) + ". "
                + "Return ONLY the translated text, no explanations.\"},"
                + "  {\"role\":\"user\",\"content\":\"" + escapedContent + "\"}"
                + "]"
                + "}";

        //write the payload to a temporary file so curl can read it cleanly
        File tempPayload = File.createTempFile("openai_payload_", ".json");
        tempPayload.deleteOnExit();
        Files.writeString(tempPayload.toPath(), jsonPayload);


        //call with curl and the process builder
        System.out.println("\nTranslating to " + targetLanguage + " …");

        ProcessBuilder pb = new ProcessBuilder(
                "curl",
                "--silent",
                "--show-error",
                "-X", "POST",
                "https://api.openai.com/v1/chat/completions",
                "-H", "Content-Type: application/json",
                "-H", "Authorization: Bearer " + apiKey,
                "--data-binary", "@" + tempPayload.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

        //read the full response
        String response;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            response = sb.toString();
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("curl exited with code " + exitCode);
            System.err.println("Response: " + response);
            System.exit(1);
        }


        String translatedText = extractTranslatedText(response);
        if (translatedText == null) {
            System.err.println("Error: could not parse the API response.");
            System.err.println("Raw response:\n" + response);
            System.exit(1);
        }

        //write to a new file
        String outputPath = buildOutputPath(inputPath, targetLanguage);
        Files.writeString(Path.of(outputPath), translatedText);

        System.out.println("Translation complete");
        System.out.println("Output file: " + outputPath);
    }



    //functions


    private static String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n")
                .replace("\t", "\\t");
    }


    private static String extractTranslatedText(String json) {
        //find the last "content": field (which is the reply)
        int idx = json.lastIndexOf("\"content\":");
        if (idx == -1) return null;

        // skip to the translated part of the text
        int start = json.indexOf("\"", idx + 10);
        if (start == -1) return null;
        start++;

        // Walk forward, respecting escape sequences
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '"' -> { sb.append('"');  i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    default  -> { sb.append(next); i++; }
                }
            } else if (c == '"') {
                break;  // closing quote
            } else {
                sb.append(c);
            }
        }

        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }


    private static String buildOutputPath(String inputPath, String language) {
        String sanitized = language.replace(" ", "_");
        if (inputPath.endsWith(".txt")) {
            return inputPath.substring(0, inputPath.length() - 4) + "_" + sanitized + ".txt";
        }
        return inputPath + "_" + sanitized + ".txt";
    }
}