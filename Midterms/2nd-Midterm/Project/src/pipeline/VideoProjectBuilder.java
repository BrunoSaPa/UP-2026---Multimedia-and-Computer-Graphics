package pipeline;

import model.MediaContent;
import model.TravelSequence;
import service.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline steps:
 *
 *   Scan input directory and extract metadata via exiftool
 *   Generate AI descriptions for each media item
 *   Resolve GPS coordinates to human-readable place names
 *   Extract journey mood from all moments
 *   Generate the AI essence intro image
 *   Generate inspiration phrase (based on ALL locations)
 *   Generate the map closing image
 *   Generate TTS narration for each segment via OpenAI TTS
 *   Adjust all media to portrait mode (1080x1920)
 *   Assemble the final video
 */
public class VideoProjectBuilder {

    private final ExifToolService exifToolService;
    private final OpenAIService openAIService;
    private final OpenAITTSService ttsService;
    private final MapService mapService;
    private final FFmpegService ffmpegService;
    private final File workDir;

    public VideoProjectBuilder(
            ExifToolService exifToolService,
            OpenAIService openAIService,
            OpenAITTSService ttsService,
            MapService mapService,
            FFmpegService ffmpegService,
            File workDir) {
        this.exifToolService = exifToolService;
        this.openAIService = openAIService;
        this.ttsService = ttsService;
        this.mapService = mapService;
        this.ffmpegService = ffmpegService;
        this.workDir = workDir;
    }

    //builds complete video from a directory of media files
    public File build(File inputDir, File outputFile) throws IOException, InterruptedException {
        printBanner("travel video builder");

        // Step 1: Scan and extract metadata
        TravelSequence sequence = scanAndExtractMetadata(inputDir);

        // Step 2: Generate AI descriptions
        generateDescriptions(sequence);

        // Step 3: Resolve location names
        resolveLocationNames(sequence);

        // Step 4: Extract journey mood
        String journeyMood = extractJourneyMood(sequence);

        // Step 5: Generate essence intro image
        generateIntroImage(sequence, journeyMood);

        // Step 6: Generate inspiration phrase from ALL locations
        generateInspirationPhrase(sequence);

        // Step 7: Generate map closing image with text overlay
        generateMapImage(sequence);

        // Step 8: Generate TTS narration audio
        List<File> narrationFiles = generateNarration(sequence);

        // Step 9: Adjust all media and add narration
        List<File> videoSegments = processMediaSegments(sequence, narrationFiles);

        // Step 10: Assemble final video
        File finalVideo = assembleVideo(sequence, videoSegments, outputFile);

        printBanner("Pipeline Complete\n  Output: " + finalVideo.getAbsolutePath());
        return finalVideo;
    }

    //steps


    //scan the directory and extract metadata from the media files
    private TravelSequence scanAndExtractMetadata(File inputDir){

        System.out.println("[Step 1/10] Scanning media files and extracting metadata...");
        TravelSequence sequence = new TravelSequence();

        File[] files = inputDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".heic") || lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi") || lower.endsWith(".mkv");
        });

        if (files == null || files.length == 0) {
            throw new RuntimeException("No media files found in: " + inputDir.getAbsolutePath());
        }

        for (File file : files) {
            try {
                MediaContent media = exifToolService.extractMetadata(file);
                sequence.addMedia(media);
                System.out.println("  + " + file.getName() + "  " + media);
            } catch (Exception e) {
                System.err.println("skipping " + file.getName() + ": " + e.getMessage());
            }
        }

        if (sequence.isEmpty()) {
            throw new RuntimeException("no valid media files could be processed");
        }

        System.out.println("Found" + sequence.size() + " media files\n");
        return sequence;
    }


    //generate descriptions with ai for each media file
    private void generateDescriptions(TravelSequence sequence){

        System.out.println("[Step 2/10] Generating AI descriptions...");
        for (MediaContent media : sequence.getSortedMedia()) {
            try {
                String description = openAIService.describeContent(media);
                media.setAiDescription(description);
                System.out.println("  + " + media.getFile().getName() + ": "
                        + truncate(description, 80));
            } catch (Exception e) {
                System.err.println("Description failed for "
                        + media.getFile().getName() + ": " + e.getMessage());
                media.setAiDescription("A moment captured during the journey.");
            }
        }
        System.out.println();
    }


    //resolve gps coordinate to something human can name, because one of the requirements said that the places had to be recognizable
    private void resolveLocationNames(TravelSequence sequence){

        System.out.println("[Step 3/10] Resolving location names...");
        for (MediaContent media : sequence.getSortedMedia()) {
            if (media.hasGPSData() && media.getLocation().getPlaceName() == null) {
                try {
                    String name = openAIService.resolveLocationName(media.getLocation());
                    media.getLocation().setPlaceName(name);
                    System.out.println("  + " + media.getLocation().toCoordinateString()
                            + " -> " + name);
                } catch (Exception e) {
                    System.err.println("Could not resolve location: " + e.getMessage());
                }
            }
        }
        System.out.println();
    }

    //derives mood from the descriptions, so it can be used to create the first image
    private String extractJourneyMood(TravelSequence sequence)
            throws IOException, InterruptedException {
        System.out.println("[Step 4/10] Extracting journey mood...");
        try {
            String mood = openAIService.extractJourneyMood(sequence.getSortedMedia());
            System.out.println("  + Mood: " + mood + "\n");
            return mood;
        } catch (Exception e) {
            System.err.println("Mood extraction failed: " + e.getMessage());
            String fallback = "calm, exploratory, cinematic";
            System.err.println("using fallback mood: " + fallback + "\n");
            return fallback;
        }
    }


    //generates the image from for the intro
    private void generateIntroImage(TravelSequence sequence, String journeyMood) throws IOException, InterruptedException {

        System.out.println("[Step 5/10] Generating essence intro image...");
        File introImage = openAIService.generateEssenceImage(sequence.getSortedMedia(), journeyMood);
        sequence.setIntroImage(introImage);
        System.out.println("intro image saved: " + introImage.getName() + "\n");
    }


    //generates phrase based on the locations
    private void generateInspirationPhrase(TravelSequence sequence){

        System.out.println("[Step 6/10] Generating inspiration phrase from all locations...");
        try {
            String phrase = openAIService.generateInspirationPhrase(sequence.getAllLocations());
            sequence.setInspirationPhrase(phrase);
            System.out.println("  + \"" + phrase + "\"\n");
        } catch (Exception e) {
            sequence.setInspirationPhrase("Every journey tells a story.");
            System.err.println("using fallback phrase: " + e.getMessage() + "\n");
        }
    }


    //generates closing map image with locations and text
    private void generateMapImage(TravelSequence sequence)
            throws IOException, InterruptedException {

        System.out.println("[Step 7/10] Generating map image...");
        List<model.GPSLocation> locations = sequence.getAllLocations();
        if (locations.size() < 2) {
            System.err.println("map generation skipped, no GPS tagged media available\n");
            return;
        }
        File mapImage = mapService.generateMapImage(locations, sequence.getInspirationPhrase());
        sequence.setMapImage(mapImage);
        System.out.println("  + Map image saved: " + mapImage.getName() + "\n");
    }

    //generates tts narration based on media description
    private List<File> generateNarration(TravelSequence sequence)
            throws IOException, InterruptedException {

        System.out.println("[Step 8/10] Generating TTS narration...");
        List<File> narrationFiles = new ArrayList<>();
        List<MediaContent> sortedMedia = sequence.getSortedMedia();

        for (int i = 0; i < sortedMedia.size(); i++) {
            MediaContent media = sortedMedia.get(i);
            String description = media.getAiDescription() != null
                    ? media.getAiDescription()
                    : "A moment from the journey.";
            File audioFile = ttsService.synthesize(description, "narration_" + i);
            narrationFiles.add(audioFile);
            System.out.println("  + Narration " + (i + 1) + "/" + sortedMedia.size());
        }
        System.out.println();
        return narrationFiles;
    }


    //adjusts the media to portrait mode and overlays the narration audio
    private List<File> processMediaSegments(TravelSequence sequence, List<File> narrationFiles) {

        System.out.println("[Step 9/10] Processing media segments...");
        List<File> videoSegments = new ArrayList<>();
        List<MediaContent> sortedMedia = sequence.getSortedMedia();

        for (int i = 0; i < sortedMedia.size(); i++) {
            MediaContent media = sortedMedia.get(i);
            try {
                //adjust orientation and size to portrait
                File adjusted = ffmpegService.adjustMedia(media);
                System.out.println("  + Adjusted: " + media.getFile().getName());

                if (i >= narrationFiles.size() || narrationFiles.get(i) == null) {
                    throw new RuntimeException("missing narration for segment " + (i + 1));
                }
                adjusted = ffmpegService.addAudioNarration(adjusted, narrationFiles.get(i));
                System.out.println("    + Added narration");

                videoSegments.add(adjusted);
            } catch (Exception e) {
                System.err.println("failed to proces " + media.getFile().getName()
                        + ": " + e.getMessage());
            }
        }
        System.out.println();
        return videoSegments;
    }


    //step 10, assemble al segments into the final vido
    private File assembleVideo(TravelSequence sequence, List<File> mediaSegments, File outputFile) throws IOException, InterruptedException {

        System.out.println("[Step 10/10] Assembling final video...");
        List<File> allSegments = new ArrayList<>();

        // 1. intro image segment (ai generated essence)
        if (sequence.getIntroImage() == null) {
            throw new RuntimeException("AI intro image is required but was not generated.");
        }
        File introSegment = ffmpegService.imageToVideo(
                sequence.getIntroImage(), 5, "intro_segment");
        allSegments.add(introSegment);
        System.out.println("  + Added intro segment");

        // 2. all media segments (already sorted by date)
        allSegments.addAll(mediaSegments);
        System.out.println("  + Added " + mediaSegments.size() + " media segments");

        // 3. map closing segment with inspirational phrase
        if (sequence.getMapImage() != null) {
            File mapSegment = ffmpegService.imageToVideo(
                    sequence.getMapImage(), 8, "map_segment");
            allSegments.add(mapSegment);
            System.out.println("  + Added map closing segment");
        }

        if (allSegments.isEmpty()) {
            throw new RuntimeException("Pipeline produced no usable segments to assemble.");
        }

        //assemble all segments
        File assembled = ffmpegService.assembleVideo(allSegments, outputFile.getName());

        //copy to the desired output location if different from workDir
        if (!assembled.getAbsolutePath().equals(outputFile.getAbsolutePath())) {
            Files.copy(assembled.toPath(), outputFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        return outputFile;
    }

    //utility

    private void printBanner(String message) {
        System.out.println("  " + message);
    }

    //no need to write everything in the console
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
