package functions;

import bean.StationPeopleNumber;
import com.google.gson.Gson;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pool.RedisPool;
import redis.clients.jedis.Jedis;
import scala.Tuple2;

/**
 * @ClassName SaveNumberMaxMin
 * @Author DragonistYJ
 * @Date 2020/3/5
 * @Description 保存最大和最小的人数
 */
public class SaveNumberMaxMin implements VoidFunction<Tuple2<String, StationPeopleNumber>> {
    private String suffix;
    private Broadcast<RedisPool> redisPoolBroadcast;
    private Logger logger;

    public SaveNumberMaxMin() {
    }

    public SaveNumberMaxMin(String suffix, Broadcast<RedisPool> redisPoolBroadcast) {
        this.suffix = suffix;
        this.redisPoolBroadcast = redisPoolBroadcast;
        this.logger = LoggerFactory.getLogger(SaveNumberMaxMin.class);
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void call(Tuple2<String, StationPeopleNumber> tuple2) {
        StationPeopleNumber stationPeopleNumber = tuple2._2();
        String key = tuple2._1() + suffix;
        Gson gson = new Gson();
        RedisPool redisPool = redisPoolBroadcast.getValue();
        try {
            Jedis jedis = redisPool.getConnection();
            jedis.set(key, String.valueOf(stationPeopleNumber.getPeopleNumber()));
            redisPool.release(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        logger.info(key + " " + stationPeopleNumber.toString());
    }
}
