package bean;

import java.io.Serializable;
import java.util.Objects;

public class LatLng implements Serializable {
    private Double longitude;
    private Double latitude;

    public LatLng(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public LatLng() {
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LatLng latLng = (LatLng) o;
        return Objects.equals(longitude, latLng.longitude) &&
                Objects.equals(latitude, latLng.latitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude);
    }

    @Override
    public String toString() {
        return "LatLng{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
