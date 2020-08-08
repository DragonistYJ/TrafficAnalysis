package functions;

import bean.BaseStation;
import bean.Migration;
import bean.StationHourReport;
import com.google.gson.Gson;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pool.RedisPool;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName SaveHourReport
 * @Author DragoinstYJ
 * @Date 2020/3/6
 * @Description 保存每个基站的小时报表
 */
public class SaveHourReport implements VoidFunction<Iterator<BaseStation>> {
    private Broadcast<RedisPool> redisPoolBroadcast;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String timeTag;
    private Logger logger;

    public SaveHourReport(Broadcast<RedisPool> redisPoolBroadcast, Calendar calendar) {
        this.redisPoolBroadcast = redisPoolBroadcast;
        this.calendar = calendar;
        this.dateFormat = new SimpleDateFormat("YYYY-MM-dd-HH");
        this.timeTag = this.dateFormat.format(calendar.getTime());
        this.logger = LoggerFactory.getLogger(SaveHourReport.class);
    }

    @Override
    public void call(Iterator<BaseStation> baseStationIterator) {
        Gson gson = new Gson();
        try {
            RedisPool redisPool = redisPoolBroadcast.getValue();
            Jedis jedis = redisPool.getConnection();
            while (baseStationIterator.hasNext()) {
                BaseStation baseStation = baseStationIterator.next();
                baseStation.slim();
                String stationID = baseStation.getLac() + "-" + baseStation.getCell();
                if (!jedis.exists(stationID)) continue;

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(jedis.get(stationID + ".firstTime")));
                String firstTime = this.dateFormat.format(calendar.getTime());
                if (firstTime.compareTo(timeTag) > 0) continue;

                StationHourReport stationHourReport = new StationHourReport();
                stationHourReport.setBaseStation(baseStation);
                stationHourReport.setInflow(new ArrayList<>());
                stationHourReport.setOutflow(new ArrayList<>());

                calendar.setTimeInMillis(this.calendar.getTimeInMillis());
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                int previousPeopleNumber = getPreviousPeopleNumber(stationID, jedis);
                jedis.set(stationID + "." + timeTag + ".peopleNumber", String.valueOf(previousPeopleNumber));
                if (jedis.exists(stationID + ".max")) {
                    stationHourReport.setMaxPeopleNumber(Integer.valueOf(jedis.get(stationID + ".max")));
                    stationHourReport.setMinPeopleNumber(Integer.valueOf(jedis.get(stationID + ".min")));
                } else {
                    stationHourReport.setMaxPeopleNumber(previousPeopleNumber);
                    stationHourReport.setMinPeopleNumber(previousPeopleNumber);
                }

                if (jedis.exists(stationID + ".inflow")) {
                    List<String> stringList = jedis.lrange(stationID + ".inflow", 0, -1);
                    for (String s : stringList) {
                        Migration migration = gson.fromJson(s, Migration.class);
                        migration.setToStation(null);
                        stationHourReport.getInflow().add(migration);
                    }
                }
                if (jedis.exists(stationID + ".outflow")) {
                    List<String> stringList = jedis.lrange(stationID + ".outflow", 0, -1);
                    for (String s : stringList) {
                        Migration migration = gson.fromJson(s, Migration.class);
                        migration.setFromStation(null);
                        stationHourReport.getOutflow().add(migration);
                    }
                }

                jedis.rpush(timeTag + ".hourReport", gson.toJson(stationHourReport));
                redisPool.release(jedis);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发生错误");
        }
    }

    /**
     * 获取要求时间下，基站最后的承载人数
     *
     * @param stationID 基站标识符
     * @param jedis     Redis连接
     * @return 要求时间下的人数
     */
    public int getPreviousPeopleNumber(String stationID, Jedis jedis) {
        String timeTag = this.dateFormat.format(calendar.getTime());
        if (jedis.exists(stationID + "." + timeTag + ".peopleNumber")) {
            return Integer.parseInt(jedis.get(stationID + "." + timeTag + ".peopleNumber"));
        } else {
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            int previousPeopleNumber = getPreviousPeopleNumber(stationID, jedis);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            return previousPeopleNumber;
        }
    }
}