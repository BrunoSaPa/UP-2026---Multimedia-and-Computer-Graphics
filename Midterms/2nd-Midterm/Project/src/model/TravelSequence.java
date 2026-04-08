package model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


//collection of the pieces of media (object MediaContent) that make up a travel sequence, along with some additional info like an intro image, map image, and an inspirational phrase
//content is automatically orderred by the date taken
public class TravelSequence {

    private final List<MediaContent> mediaList;
    private File introImage;
    private File mapImage;
    private String inspirationPhrase;

    public TravelSequence() {
        this.mediaList = new ArrayList<>();
    }

    //using a method in MediaContent to compare explicitly to dates, so when using .sort it knows what to sort for
    public void addMedia(MediaContent media) {
        mediaList.add(media);
        Collections.sort(mediaList);
    }

    public List<MediaContent> getSortedMedia() {
        return Collections.unmodifiableList(mediaList);
    }

    public int size() {
        return mediaList.size();
    }

    public boolean isEmpty() {
        return mediaList.isEmpty();
    }


    //returns alist of all the gps locations in the media
    public List<GPSLocation> getAllLocations() {
        return mediaList.stream()
                .filter(MediaContent::hasGPSData)
                .map(MediaContent::getLocation)
                .collect(Collectors.toList());
    }

    public File getIntroImage() {
        return introImage;
    }

    public void setIntroImage(File introImage) {
        this.introImage = introImage;
    }

    public File getMapImage() {
        return mapImage;
    }

    public void setMapImage(File mapImage) {
        this.mapImage = mapImage;
    }

    public String getInspirationPhrase() {
        return inspirationPhrase;
    }

    public void setInspirationPhrase(String phrase) {
        this.inspirationPhrase = phrase;
    }
}
