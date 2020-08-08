package pool;

import redis.clients.jedis.Jedis;

import java.io.Serializable;

/**
 * @ClassName RedisPool
 * @Author Dragonist
 * @Date 2020/3/2
 * @Description Redis连接池
 */
public class RedisPool extends SimpleConnectionPool<Jedis> implements Serializable {
    public RedisPool(int maxActive, long maxWait, String poolName) {
        super(maxActive, maxWait, poolName);
    }

    @Override
    public synchronized void close() {
        if (isClosed.compareAndSet(false, true)) {
            for (Jedis jedis : freeQueue) {
                jedis.close();
            }
            for (Jedis jedis : busyQueue) {
                jedis.close();
            }
        }
    }

    @Override
    public Jedis creatSingleConnection() {
        return new Jedis("redis", 6379);
    }
}
