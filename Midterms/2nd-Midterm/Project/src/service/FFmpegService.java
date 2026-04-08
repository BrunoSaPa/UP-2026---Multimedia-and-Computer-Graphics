package service;

import model.MediaContent;
import model.MediaType;
import util.ProcessRunnerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//this class is used to proccess media files with ffmpeg, so it handles orientation correction, resizing, the narration overlay and the final video assembly aswell
public class FFmpegService {

    //portrait mode dimensions (9:16 aspect ratio)
    private static final int TARGET_WIDTH = 1080;
    private static final int TARGET_HEIGHT = 1920;

    //duration in seconds each photo is displayed
    private static final int PHOTO_DISPLAY_DURATION = 5;

    private final File workDir;


    //Workdir is a temporary directory for files needed to construct the final video
    public FFmpegService(File workDir) {
        this.workDir = workDir;
    }


    //adjust media file to portrait orientation, if it's a photo it creates a video segment with the photo displayed for a few seconds, if it's a video it just adjusts the orientation and size, in both cases it returns an mp4 file
    public File adjustMedia(MediaContent media) throws IOException, InterruptedException {
        File outputFile = new File(workDir, "adjusted_" + getBaseName(media.getFile()) + ".mp4");

        String scaleFilter = String.format(
                "scale=%d:%d:force_original_aspect_ratio=increase,crop=%d:%d,setsar=1",
                TARGET_WIDTH, TARGET_HEIGHT, TARGET_WIDTH, TARGET_HEIGHT
        );

        List<String> command;
        if (media.getType() == MediaType.PHOTO) {
            command = Arrays.asList(
                    "ffmpeg", "-y",
                    "-loop", "1",
                    "-i", media.getFile().getAbsolutePath(),
                    "-t", String.valueOf(PHOTO_DISPLAY_DURATION),
                    "-vf", scaleFilter,
                    "-c:v", "libx264",
                    "-pix_fmt", "yuv420p",
                    "-r", "30",
                    outputFile.getAbsolutePath()
            );
        } else {
            command = Arrays.asList(
                    "ffmpeg", "-y",
                    "-i", media.getFile().getAbsolutePath(),
                    "-vf", scaleFilter,
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-pix_fmt", "yuv420p",
                    "-r", "30",
                    outputFile.getAbsolutePath()
            );
        }

        ProcessRunnerUtil.runOrThrow(command);
        return outputFile;
    }

    //creates a video segment from an image, used for the intro and the map
    public File imageToVideo(File imageFile, int duration, String name)
            throws IOException, InterruptedException {

        File outputFile = new File(workDir, name + ".mp4");

        String scaleFilter = String.format(
                "scale=%d:%d:force_original_aspect_ratio=increase,crop=%d:%d,setsar=1",
                TARGET_WIDTH, TARGET_HEIGHT, TARGET_WIDTH, TARGET_HEIGHT
        );

        List<String> command = Arrays.asList(
                "ffmpeg", "-y",
                "-loop", "1",
                "-i", imageFile.getAbsolutePath(),
                "-f", "lavfi",
                "-i", "anullsrc=r=44100:cl=stereo",
                "-t", String.valueOf(duration),
                "-vf", scaleFilter,
                "-map", "0:v",
                "-map", "1:a",
                "-c:v", "libx264",
                "-c:a", "aac",
                "-pix_fmt", "yuv420p",
                "-r", "30",
                "-shortest",
                outputFile.getAbsolutePath()
        );

        ProcessRunnerUtil.runOrThrow(command);
        return outputFile;
    }


    //overlays narration audio onto a video segment, if the audio is longer than the video it extends the video by cloning the last frame, if the video is longer than the audio it pads the audio with silence, in both cases it returns an mp4 file
    public File addAudioNarration(File videoFile, File audioFile)
            throws IOException, InterruptedException {

        File outputFile = new File(workDir, "narrated_" + videoFile.getName());

        double audioDuration = getMediaDuration(audioFile);
        double videoDuration = getMediaDuration(videoFile);

        List<String> command;
        if (audioDuration > videoDuration) {
            //extend video to match audio length using tpad (clone last frame)
            command = Arrays.asList(
                    "ffmpeg", "-y",
                    "-i", videoFile.getAbsolutePath(),
                    "-i", audioFile.getAbsolutePath(),
                    "-filter_complex",
                    "[0:v]tpad=stop_mode=clone:stop_duration="
                            + (int) Math.ceil(audioDuration - videoDuration) + "[v]",
                    "-map", "[v]",
                    "-map", "1:a",
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-ar", "44100",
                    "-ac", "2",
                    "-b:a", "128k",
                    "-shortest",
                    outputFile.getAbsolutePath()
            );
        } else {
            command = Arrays.asList(
                    "ffmpeg", "-y",
                    "-i", videoFile.getAbsolutePath(),
                    "-i", audioFile.getAbsolutePath(),
                    "-map", "0:v",
                    "-map", "1:a",
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-ar", "44100",
                    "-ac", "2",
                    "-b:a", "128k",
                    "-af", "apad",
                    "-shortest",
                    outputFile.getAbsolutePath()
            );
        }

        ProcessRunnerUtil.runOrThrow(command);
        return outputFile;
    }


    //concatenates multiple video segments into a final single video
    public File assembleVideo(List<File> segments, String outputName)
            throws IOException, InterruptedException {

        if (segments == null || segments.isEmpty()) {
            throw new RuntimeException("No video segments available for final assembly.");
        }

        File outputFile = new File(workDir, outputName);

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");

        for (File segment : segments) {
            command.add("-i");
            command.add(segment.getAbsolutePath());
        }

        StringBuilder filter = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            filter.append("[").append(i).append(":v:0]");
            filter.append("[").append(i).append(":a:0]");
        }
        filter.append("concat=n=").append(segments.size()).append(":v=1:a=1[v][a]");

        command.add("-filter_complex");
        command.add(filter.toString());
        command.add("-map");
        command.add("[v]");
        command.add("-map");
        command.add("[a]");
        command.add("-c:v");
        command.add("libx264");
        command.add("-c:a");
        command.add("aac");
        command.add("-ar");
        command.add("44100");
        command.add("-ac");
        command.add("2");
        command.add("-b:a");
        command.add("128k");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add(outputFile.getAbsolutePath());

        ProcessRunnerUtil.runOrThrow(command);
        return outputFile;
    }


    //gets the duration of a media file in secods using ffprobe
    private double getMediaDuration(File file) throws IOException, InterruptedException {
        List<String> command = Arrays.asList(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                file.getAbsolutePath()
        );

        ProcessRunnerUtil.ProcessResult result = ProcessRunnerUtil.run(command);
        try {
            return Double.parseDouble(result.getStdout().trim());
        } catch (NumberFormatException e) {
            return PHOTO_DISPLAY_DURATION;
        }
    }

    //extracts the base name from a file without the extension
    private String getBaseName(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return dotIndex > 0 ? name.substring(0, dotIndex) : name;
    }
}
