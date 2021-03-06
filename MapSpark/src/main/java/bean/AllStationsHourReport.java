package bean;

import java.util.List;

/**
 * @ClassName StationHourReport
 * @Author DragonistYJ
 * @Date 2020/3/6
 * @Description 基站信息小时报表
 */
public class AllStationsHourReport {
    private String _id;
    private List<StationHourReport> hourReports;

    public AllStationsHourReport() {
    }

    public AllStationsHourReport(String _id, List<StationHourReport> hourReports) {
        this._id = _id;
        this.hourReports = hourReports;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public List<StationHourReport> getHourReports() {
        return hourReports;
    }

    public void setHourReports(List<StationHourReport> hourReports) {
        this.hourReports = hourReports;
    }
}
