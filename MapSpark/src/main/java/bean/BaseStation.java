package bean;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName BaseStation
 * @Author DragonistYJ
 * @Date 2020/3/2
 * @Description 基站信息实体类
 */
public class BaseStation implements Serializable {
    private String _id;
    private String lac;
    private String cell;
    // 基站经纬度
    private LatLng center;
    //泰森多边形顶点
    private List<LatLng> voronois;

    public BaseStation slim() {
        this._id = null;
        this.voronois = null;
        return this;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
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
                "_id='" + _id + '\'' +
                ", lac='" + lac + '\'' +
                ", cell='" + cell + '\'' +
                ", center=" + center +
                ", voronois=" + voronois +
                '}';
    }
}
