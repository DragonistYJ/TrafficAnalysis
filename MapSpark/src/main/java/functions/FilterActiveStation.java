package functions;

import bean.BaseStation;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pool.RedisPool;
import redis.clients.jedis.Jedis;


/**
 * @ClassName FilterActiveStation
 * @Author DragonistYJ
 * @Date 2020/3/6
 * @Description 过滤已经有人来过的基站
 */
public class FilterActiveStation implements Function<BaseStation, Boolean> {
    private Broadcast<RedisPool> redisPoolBroadcast;
    private Logger logger;

    public FilterActiveStation(Broadcast<RedisPool> redisPoolBroadcast) {
        this.redisPoolBroadcast = redisPoolBroadcast;
        this.logger = LoggerFactory.getLogger(FilterActiveStation.class);
    }

    @Override
    public Boolean call(BaseStation baseStation) {
        RedisPool redisPool = redisPoolBroadcast.getValue();
        boolean flag = false;
        try {
            Jedis jedis = redisPool.getConnection();
            String stationID = baseStation.getLac() + "-" + baseStation.getCell();
            flag = jedis.exists(stationID);
            redisPool.release(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("获取Redis连接异常");
        }
        return flag;
    }
}
