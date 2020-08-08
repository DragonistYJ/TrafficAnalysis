package love.mmjj.server.bean;

import java.util.Objects;

public class StationPeopleNumber {
    private String lac;
    private String cell;
    private Integer peopleNumber;
    private LatLng center;

    public StationPeopleNumber() {
    }

    public StationPeopleNumber setBySignaling(Signaling signaling) {
        this.lac = signaling.getLac();
        this.cell = signaling.getCell();
        return this;
    }

    public StationPeopleNumber setByBaseStation(BaseStation baseStation) {
        this.center = baseStation.getCenter();
        return this;
    }

    public String getLac() {
        return lac;
    }

    public void setLac(String lac) {
        this.lac = lac;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public Integer getPeopleNumber() {
        return peopleNumber;
    }

    public void setPeopleNumber(Integer peopleNumber) {
        this.peopleNumber = peopleNumber;
    }

    public LatLng getCenter() {
        return center;
    }

    public void setCenter(LatLng center) {
        this.center = center;
    }

    @Override
    public String toString() {
        return "StationPeopleNumber{" +
                "lac='" + lac + '\'' +
                ", cell='" + cell + '\'' +
                ", peopleNumber=" + peopleNumber +
                ", center=" + center +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StationPeopleNumber that = (StationPeopleNumber) o;
        return Objects.equals(lac, that.lac) &&
                Objects.equals(cell, that.cell) &&
                Objects.equals(center, that.center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lac, cell, center);
    }
}
