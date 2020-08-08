package bean;

import java.io.Serializable;
import java.util.List;

public class BaseStation implements Serializable {
    private String id;
    private String lac;
    private String cell;
    private LatLng center;
    private List<LatLng> voronois;

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

    public LatLng getCenter() {
        return center;
    }

    public void setCenter(LatLng center) {
        this.center = center;
    }

    public List<LatLng> getVoronois() {
        return voronois;
    }

    public void setVoronois(List<LatLng> voronois) {
        this.voronois = voronois;
    }

    @Override
    public String toString() {
        return "BaseStation{" +
                "lac='" + lac + '\'' +
                ", cell='" + cell + '\'' +
                ", center=" + center +
                ", voronois=" + voronois +
                '}';
    }
}
