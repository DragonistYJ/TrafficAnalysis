package love.mmjj.server.bean;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document("BaseStation")
public class BaseStation implements Serializable {
    @Id
    private String _id;
    private String lac;
    private String cell;
    private LatLng center;
    private List<LatLng> voronois;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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
