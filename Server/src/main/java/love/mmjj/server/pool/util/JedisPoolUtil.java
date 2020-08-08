package love.mmjj.server.pool.util;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtil {
    private static JedisPool jedisPool;

    public synchronized static JedisPool getJedisPoolInstance() {
        if (jedisPool == null) {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(16);
            jedisPoolConfig.setMaxIdle(8);
            jedisPoolConfig.setMaxWaitMillis(5000);
            jedisPoolConfig.setTestOnBorrow(true);
            jedisPool = new JedisPool(jedisPoolConfig, "redis", 6379);
        }
        return jedisPool;
    }
}
