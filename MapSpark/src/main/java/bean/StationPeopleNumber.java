package bean;

import java.io.Serializable;

/**
 * @ClassName StationPeopleNumber
 * @Author DragonistYJ
 * @Date 2020/3/2
 * @Description 基站经纬度以及对应的人数
 */
public class StationPeopleNumber implements Serializable {
    private String lac;
    private String cell;
    private Integer peopleNumber;
    private LatLng center;

    public StationPeopleNumber() {
    }

    public StationPeopleNumber setByBaseStation(BaseStation baseStation) {
        this.lac = baseStation.getLac();
        this.cell = baseStation.getCell();
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
}
