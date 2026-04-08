package service;

import model.GPSLocation;
import model.MediaContent;
import util.ProcessRunnerUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

//this class is used to extract data from the inputs, such as GPS, size, orientation, and date with exiftool
public class ExifToolService {

    private static final DateTimeFormatter EXIF_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");


    //extracts metadata from the media file and populates the MediaContent object with it , handling exceptions
    public MediaContent extractMetadata(File file) throws IOException, InterruptedException {
        List<String> command = Arrays.asList(
                "exiftool", "-n", "-T",
                "-GPSLatitude", "-GPSLongitude",
                "-DateTimeOriginal", "-CreateDate",
                "-ImageWidth", "-ImageHeight",
                "-Rotation", "-Orientation",
                file.getAbsolutePath()
        );

        ProcessRunnerUtil.ProcessResult result = ProcessRunnerUtil.runOrThrow(command);
        String[] fields = result.getStdout().strip().split("\\t", -1);

        if (fields.length < 8) {
            throw new RuntimeException("no metadata found for " + file.getName());
        }

        MediaContent media = new MediaContent(file);
        parseGPSData(fields, media);
        parseDateTaken(fields, media);
        parseDimensions(fields, media);
        parseOrientation(fields, media);

        return media;
    }


    //converts gps data from exiftool output to a GPSLocation object and sets it in the MediaContent, if the data is valid
    private void parseGPSData(String[] fields, MediaContent media) {
        Double lat = parseDoubleField(fields[0]);
        Double lon = parseDoubleField(fields[1]);
        if (lat != null && lon != null) {
            media.setLocation(new GPSLocation(lat, lon));
        }
    }

    //parses date from exiftool and sets it in the MediaContent, if the data is valid, it tries both DateTimeOriginal and CreateDate since some media only have one of them
    private void parseDateTaken(String[] fields, MediaContent media) {
        String dateStr = preferredField(fields[2], fields[3]);
        if (dateStr != null) {
            try {
                media.setDateTaken(LocalDateTime.parse(dateStr.trim(), EXIF_DATE_FORMAT));
            } catch (DateTimeParseException e) {
                System.err.println("could not parse date '" + dateStr + "' for " + media.getFile().getName());
            }
        }
    }

    //parses media dimensions from exiftool output and sets them in the MediaContent, if the data is valid
    private void parseDimensions(String[] fields, MediaContent media) {
        Integer width = parseIntField(fields[4]);
        Integer height = parseIntField(fields[5]);
        if (width != null) media.setWidth(width);
        if (height != null) media.setHeight(height);
    }

    //concerts orientation data so we can later construct with correct orientation in ffmpeg service
    private void parseOrientation(String[] fields, MediaContent media) {
        Integer orientation = parseIntField(fields[7]);
        if (orientation != null) {
            media.setOrientation(orientation);
        } else {
            Integer rotation = parseIntField(fields[6]);
            if (rotation != null) {
                media.setOrientation(rotationToOrientation(rotation));
            }
        }
    }

    private String preferredField(String primary, String fallback) {
        if (!isMissing(primary)) {
            return primary;
        }
        return isMissing(fallback) ? null : fallback;
    }

    private Double parseDoubleField(String value) {
        if (isMissing(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntField(String value) {
        if (isMissing(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank() || "-".equals(value.trim());
    }


    //since exif doesnt take degrees we have to convert them to know the rotation in something exif understands (orientation)
    private int rotationToOrientation(int rotation) {
        switch (rotation) {
            case 90:  return 6;
            case 180: return 3;
            case 270: return 8;
            default:  return 1;
        }
    }
}
