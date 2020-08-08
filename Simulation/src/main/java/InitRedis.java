import bean.BaseStation;
import bean.Signaling;
import com.google.gson.Gson;
import signaling.ReadRowData;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * @ClassName InitRedis
 * @Author DragonistYJ
 * @Date 2020/3/5
 * @Description 初始化redis, 每个人的初始位置, 以及每个基站的初始人数
 */
public class InitRedis {
    public static void main(String[] args) {
        List<Signaling> signalingList = new ReadRowData().read("/rowdata.csv");
        signalingList.sort(Comparator.comparingLong(Signaling::getTimestamp));
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        Calendar calendar = Calendar.getInstance();

        Gson gson = new Gson();
        BaseStation baseStation = new BaseStation();
        baseStation.setLac("123");
        baseStation.setCell("2343");
        System.out.println(gson.toJson(baseStation));
    }
}
