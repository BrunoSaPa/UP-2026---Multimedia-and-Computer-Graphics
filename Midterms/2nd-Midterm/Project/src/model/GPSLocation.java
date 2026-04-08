package model;


public class GPSLocation {

    private final double latitude;
    private final double longitude;
    private String placeName;

    public GPSLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String toCoordinateString() {
        return latitude + "," + longitude;
    }

    @Override
    public String toString() {
        String name = placeName != null ? " (" + placeName + ")" : "";
        return String.format("GPSLocation[%.6f, %.6f%s]", latitude, longitude, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GPSLocation that = (GPSLocation) o;
        return Double.compare(that.latitude, latitude) == 0 && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        long latBits = Double.doubleToLongBits(latitude);
        long lonBits = Double.doubleToLongBits(longitude);
        return 31 * Long.hashCode(latBits) + Long.hashCode(lonBits);
    }
}

