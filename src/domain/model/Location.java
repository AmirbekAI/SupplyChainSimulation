package domain.model;

import java.util.Objects;

public class Location {
    private final String name;
    private final double latitude;
    private final double longtitude;

    public Location(String name, double lat, double lon) {
        this.name = name;
        this.latitude = lat;
        this.longtitude = lon;
    }

    public String getName() {
        return name;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double calculateDistanceTo(Location location) {
        double dx = this.latitude - location.getLatitude();
        double dy = this.longtitude - location.getLongtitude();
        return Math.sqrt((dx * dx) + (dy * dy));
    }

    @Override
    public boolean equals(Object obj) {
        if (this.equals(obj)) { return true;}
        if (obj.getClass() != this.getClass()) { return false; }

        Location location = (Location) obj;
        return Objects.equals(this.getLongtitude(), location.getLongtitude()) && Objects.equals(this.getLatitude(), location.getLatitude());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.latitude, this.longtitude);
    }
}
