package functions;

import bean.Migration;
import com.google.gson.Gson;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pool.RedisPool;
import redis.clients.jedis.Jedis;

import java.util.Iterator;

/**
 * @ClassName SaveStationMigration
 * @Author DragonistYJ
 * @Date 2020/3/6
 * @Description 保存基站每小时地迁徙信息
 */
public class SaveStationMigration implements VoidFunction<Iterator<Migration>> {
    private Broadcast<RedisPool> redisPoolBroadcast;
    private Logger logger;

    public SaveStationMigration(Broadcast<RedisPool> redisPoolBroadcast) {
        this.redisPoolBroadcast = redisPoolBroadcast;
        this.logger = LoggerFactory.getLogger(SaveStationMigration.class);
    }

    @Override
    public void call(Iterator<Migration> migrationIterator) {
        RedisPool redisPool = redisPoolBroadcast.getValue();
        Gson gson = new Gson();
        try {
            Jedis jedis = redisPool.getConnection();
            while (migrationIterator.hasNext()) {
                Migration migration = migrationIterator.next();
                String key = migration.getFromStation().getLac() + "-" + migration.getFromStation().getCell() + ".outflow";
                jedis.rpush(key, gson.toJson(migration));
                key = migration.getToStation().getLac() + "-" + migration.getToStation().getCell() + ".inflow";
                jedis.rpush(key, gson.toJson(migration));
            }
            redisPool.release(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取Redis连接发生错误");
        }
    }
}
