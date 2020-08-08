package functions;

import bean.Signaling;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pool.*;
import redis.clients.jedis.Jedis;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName HandleSignaling
 * @Author DragonistYJ
 * @Date 2020/3/9
 * @Description 处理信令数据
 */
public class HandleSignaling implements VoidFunction<JavaRDD<Signaling>> {
    private Broadcast<RedisPoolSerializable> redisBroadcast;
    private Broadcast<MongoClientSerializable> mongoBroadcast;
    private Broadcast<KafkaProducerSerializable> kafkaBroadcast;
    private Logger logger;

    public HandleSignaling(Broadcast<RedisPoolSerializable> redisBroadcast, Broadcast<MongoClientSerializable> mongoBroadcast, Broadcast<KafkaProducerSerializable> kafkaBroadcast) {
        this.redisBroadcast = redisBroadcast;
        this.mongoBroadcast = mongoBroadcast;
        this.kafkaBroadcast = kafkaBroadcast;
        this.logger = LoggerFactory.getLogger(HandleSignaling.class);
    }

    @Override
    public void call(JavaRDD<Signaling> signalingJavaRDD) throws Exception {
        signalingJavaRDD.foreachPartition(new VoidFunction<Iterator<Signaling>>() {
            @Override
            public void call(Iterator<Signaling> signalingIterator) throws Exception {
                Jedis jedis = redisBroadcast.getValue().getResource();
                MongoCollection<Document> mongoCollection = mongoBroadcast.getValue().getDatabase("traffic-analysis").getCollection("BaseStation");
                KafkaProducerSerializable kafkaProducer = kafkaBroadcast.getValue();
                SignalingHandler signalingHandler = new SignalingHandler(jedis, mongoCollection, kafkaProducer);

                while (signalingIterator.hasNext()) {
                    Signaling signaling = signalingIterator.next();
                    String stationID = signaling.getLac() + "-" + signaling.getCell();
                    // 人第一次出现
                    if (!jedis.hexists("latestSignaling", signaling.getImsi())) {
                        // 基站第一次记录人
                        if (!jedis.hexists("peopleNumber", stationID)) {
                            signalingHandler.peopleFirstStationFirst(signaling);
                        }
                        // 基站第N次记录
                        else {
                            signalingHandler.peopleFirstStationSeveral(signaling);
                        }
                    }
                    // 人第N次出现
                    else {
                        // 基站第一次记录人
                        if (!jedis.hexists("peopleNumber", stationID)) {
                            signalingHandler.peopleSeveralStationFirst(signaling);
                        }
                        // 基站第N次记录人
                        else {
                            signalingHandler.peopleSeveralStationSeveral(signaling);
                        }
                    }
                }

                logger.info("分区" + TaskContext.get().partitionId() + " 处理完毕信令");
                jedis.close();
            }
        });
    }
}
