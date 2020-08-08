package love.mmjj.server.bean;

import java.util.List;

/**
 * @ClassName StationHourReport
 * @Author DragonsitYJ
 * @Date 2020/3/6
 * @Description 一个基站信息小时报表
 */
public class StationHourReport {
    private BaseStation baseStation;
    private Integer maxPeopleNumber;
    private Integer minPeopleNumber;
    private List<Migration> inflow;
    private List<Migration> outflow;

    public StationHourReport() {
    }

    public StationHourReport(BaseStation baseStation, Integer maxPeopleNumber, Integer minPeopleNumber, List<Migration> inflow, List<Migration> outflow) {
        this.baseStation = baseStation;
        this.maxPeopleNumber = maxPeopleNumber;
        this.minPeopleNumber = minPeopleNumber;
        this.inflow = inflow;
        this.outflow = outflow;
    }

    public BaseStation getBaseStation() {
        return baseStation;
    }

    public void setBaseStation(BaseStation baseStation) {
        this.baseStation = baseStation;
    }

    public Integer getMaxPeopleNumber() {
        return maxPeopleNumber;
    }

    public void setMaxPeopleNumber(Integer maxPeopleNumber) {
        this.maxPeopleNumber = maxPeopleNumber;
    }

    public Integer getMinPeopleNumber() {
        return minPeopleNumber;
    }

    public void setMinPeopleNumber(Integer minPeopleNumber) {
        this.minPeopleNumber = minPeopleNumber;
    }

    public List<Migration> getInflow() {
        return inflow;
    }

    public void setInflow(List<Migration> inflow) {
        this.inflow = inflow;
    }

    public List<Migration> getOutflow() {
        return outflow;
    }

    public void setOutflow(List<Migration> outflow) {
        this.outflow = outflow;
    }

    @Override
    public String toString() {
        return "StationHourReport{" +
                "baseStation=" + baseStation +
                ", maxPeopleNumber=" + maxPeopleNumber +
                ", minPeopleNumber=" + minPeopleNumber +
                ", inflow=" + inflow +
                ", outflow=" + outflow +
                '}';
    }
}
