package utils;

import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import pool.KafkaProducerSerializable;
import pool.MongoClientSerializable;
import pool.RedisPoolSerializable;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName BroadCastFactory
 * @Author DragonistYJ
 * @Date 2020/3/9
 * @Description 生成广播对象（主类太长了）
 */
public class BroadCastFactory {
    private JavaSparkContext javaSparkContext;

    public BroadCastFactory(JavaSparkContext javaSparkContext) {
        this.javaSparkContext = javaSparkContext;
    }

    public Broadcast<RedisPoolSerializable> getRedisBroadcast() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(16);
        jedisPoolConfig.setMaxIdle(4);
        jedisPoolConfig.setMaxWaitMillis(1000 * 100);
        jedisPoolConfig.setTestOnBorrow(true);
        return javaSparkContext.broadcast(new RedisPoolSerializable(jedisPoolConfig, "redis", 6379));
    }

    public Broadcast<KafkaProducerSerializable> getKafkaBroadcast() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return javaSparkContext.broadcast(new KafkaProducerSerializable(props));
    }

    public Broadcast<MongoClientSerializable> getMongoBroadcast() {
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder()
                .connectionsPerHost(16)
                .maxWaitTime(5000)
                .build();
        ServerAddress address = new ServerAddress("mongodb", 27016);
        return javaSparkContext.broadcast(new MongoClientSerializable(address, mongoClientOptions));
    }

    public Broadcast<ConcurrentHashMap<String, AtomicInteger>> getNumberMapBroadcast() {
        return javaSparkContext.broadcast(new ConcurrentHashMap<>());
    }
}
