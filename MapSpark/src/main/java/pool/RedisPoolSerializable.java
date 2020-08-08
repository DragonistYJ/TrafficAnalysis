package pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

import java.io.Serializable;

/**
 * @ClassName RedisPoolSerializable
 * @Author DragonistYJ
 * @Date 2020/3/9
 * @Description Redis线程池，为了能够在spark中进行广播，实现序列化接口
 */
public class RedisPoolSerializable extends JedisPool implements Serializable {
    public RedisPoolSerializable(GenericObjectPoolConfig poolConfig, String host, int port) {
        super(poolConfig, host, port);
    }
}
