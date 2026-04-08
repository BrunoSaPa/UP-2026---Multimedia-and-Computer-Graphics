package model;

import java.io.File;
import java.time.LocalDateTime;

//used to represent an image or video with the metadata extracted
public class MediaContent implements Comparable<MediaContent> {

    private final File file;
    private final MediaType type;
    private GPSLocation location;
    private LocalDateTime dateTaken;
    private int width;
    private int height;
    private int orientation;
    private String aiDescription;

    public MediaContent(File file) {
        this.file = file;
        this.type = MediaType.fromFileName(file.getName());
    }

    public File getFile() {
        return file;
    }

    public MediaType getType() {
        return type;
    }

    public GPSLocation getLocation() {
        return location;
    }

    public LocalDateTime getDateTaken() {
        return dateTaken;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getOrientation() {
        return orientation;
    }

    public String getAiDescription() {
        return aiDescription;
    }

    public void setLocation(GPSLocation location) {
        this.location = location;
    }

    public void setDateTaken(LocalDateTime dateTaken) {
        this.dateTaken = dateTaken;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setAiDescription(String description) {
        this.aiDescription = description;
    }

    public boolean hasGPSData() {
        return location != null;
    }

    // this is used in media sorting to order by date taken, with null dates considered less than any actual date
    @Override
    public int compareTo(MediaContent other) {
        if (this.dateTaken == null && other.dateTaken == null) return 0;
        if (this.dateTaken == null) return -1;
        if (other.dateTaken == null) return 1;
        return this.dateTaken.compareTo(other.dateTaken);
    }

    @Override
    public String toString() {
        return String.format("MediaContent[%s, %s, %s, %dx%d]",file.getName(), type, dateTaken, width, height);
    }
}

