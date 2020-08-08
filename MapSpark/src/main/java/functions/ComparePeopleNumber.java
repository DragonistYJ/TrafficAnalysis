package functions;

import bean.StationPeopleNumber;
import org.apache.spark.api.java.function.Function2;

/**
 * @ClassName ComparePeopleNumber
 * @Author DragonistYJ
 * @Date 2020/3/5
 * @Description 比较两个人数大小
 */
public class ComparePeopleNumber implements Function2<StationPeopleNumber, StationPeopleNumber, StationPeopleNumber> {
    // 比大还是比小
    private boolean larger;

    public ComparePeopleNumber() {
    }

    public ComparePeopleNumber(boolean larger) {
        this.larger = larger;
    }

    public boolean isLarger() {
        return larger;
    }

    public void setLarger(boolean larger) {
        this.larger = larger;
    }

    @Override
    public StationPeopleNumber call(StationPeopleNumber stationPeopleNumber, StationPeopleNumber stationPeopleNumber2) throws Exception {
        if (larger) {
            if (stationPeopleNumber.getPeopleNumber() > stationPeopleNumber2.getPeopleNumber()) {
                return stationPeopleNumber;
            } else {
                return stationPeopleNumber2;
            }
        } else {
            if (stationPeopleNumber.getPeopleNumber() < stationPeopleNumber2.getPeopleNumber()) {
                return stationPeopleNumber;
            } else {
                return stationPeopleNumber2;
            }
        }
    }
}
