import pipeline.VideoProjectBuilder;
import service.*;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        try {
            //read arguments
            if (args.length < 1) {
                printUsage();
                System.exit(1);
            }

            File inputDir = new File(args[0]);
            if (!inputDir.isDirectory()) {
                System.err.println("Error: '" + args[0] + "' is not a valid directory.");
                System.exit(1);
            }

            //trying to read if i got an output file argument, if not i just create one in the input directory
            File outputFile = args.length > 1 ? new File(args[1]) : new File(inputDir, "travel_video.mp4");

            //load api keys
            String openAiKey = requireEnv("OpenAIToken");
            //these arent really necesarry but since i can have the option to add them, i do =)
            String openAiTextModel = envOrDefault("OPENAI_TEXT_MODEL", "gpt-4.1-mini");
            String openAiImageModel = envOrDefault("OPENAI_IMAGE_MODEL", "gpt-image-1");
            String openAiTtsModel = envOrDefault("OPENAI_TTS_MODEL", "gpt-4o-mini-tts");
            String openAiTtsVoice = envOrDefault("OPENAI_TTS_VOICE", "alloy");
            double openAiTtsSpeed = doubleEnvOrDefault("OPENAI_TTS_SPEED", 1.1);
            int openAiRetries = intEnvOrDefault("OPENAI_MAX_RETRIES", 3);
            long openAiBackoffMs = intEnvOrDefault("OPENAI_RETRY_BACKOFF_MS", 1500);

            //this is used for temp files
            File workDir = new File(inputDir, ".travel_video_work");
            if (!workDir.exists() && !workDir.mkdirs()) {
                throw new RuntimeException("Could not create work directory: " + workDir.getAbsolutePath());
            }


            ExifToolService exifToolService = new ExifToolService();
            OpenAIService   openAIService   = new OpenAIService(
                    openAiKey,
                    workDir,
                    openAiTextModel,
                    openAiImageModel,
                    openAiRetries,
                    openAiBackoffMs
            );
            OpenAITTSService ttsService = new OpenAITTSService(
                    openAiKey,
                    openAiTtsModel,
                    openAiTtsVoice,
                    openAiTtsSpeed,
                    workDir,
                    openAiRetries,
                    openAiBackoffMs
            );
            MapService      mapService      = new MapService(workDir);
            FFmpegService   ffmpegService   = new FFmpegService(workDir);

            //build the video
            VideoProjectBuilder builder = new VideoProjectBuilder(
                    exifToolService, openAIService, ttsService,
                    mapService, ffmpegService, workDir
            );

            File result = builder.build(inputDir, outputFile);
            System.out.println("\nVideo created successfully: " + result.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("\nError: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }


    //reads env vars, handling errors if not set aswell
    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            System.err.println("Error: Environment variable " + name + " is not set.");
            System.exit(1);
        }
        return value;
    }


    //helpers
    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static int intEnvOrDefault(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double doubleEnvOrDefault(String name, double defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    //when running i need 3 arguments which are input directory and output file, retries/backoff help when the API is temporarily unavailable
    private static void printUsage() {
        System.out.println("Usage: java Main <input_directory> [output_file.mp4]");
        System.out.println();
        System.out.println("Environment variables required:");
        System.out.println("OpenAIToken");
    }
}