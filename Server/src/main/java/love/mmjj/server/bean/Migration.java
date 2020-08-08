package love.mmjj.server.bean;

/**
 * @ClassName Migration
 * @Author DragonistYJ
 * @Date 2020/3/5
 * @Description 迁徙实体类
 */
public class Migration {
    private Long timestamp;
    private String imsi;
    private BaseStation fromStation;
    private BaseStation toStation;

    public Migration() {
    }

    public Migration(Long timestamp, String imsi, BaseStation fromStation, BaseStation toStation) {
        this.timestamp = timestamp;
        this.imsi = imsi;
        this.fromStation = fromStation;
        this.toStation = toStation;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public BaseStation getFromStation() {
        return fromStation;
    }

    public void setFromStation(BaseStation fromStation) {
        this.fromStation = fromStation;
    }

    public BaseStation getToStation() {
        return toStation;
    }

    public void setToStation(BaseStation toStation) {
        this.toStation = toStation;
    }

    @Override
    public String toString() {
        return "Migration{" +
                "timestamp=" + timestamp +
                ", imsi='" + imsi + '\'' +
                ", fromStation=" + fromStation +
                ", toStation=" + toStation +
                '}';
    }
}
