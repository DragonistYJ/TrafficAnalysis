import bean.Signaling;
import com.google.gson.Gson;
import functions.HandleSignaling;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import pool.KafkaProducerSerializable;
import pool.MongoClientSerializable;
import pool.RedisPoolSerializable;
import utils.BroadCastFactory;
import utils.SingleContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName HeatMapMain
 * @Author DragonistYJ
 * @Date 2020/3/9
 * @Description 热力图主类
 */
public class HeatMapMain {
    public static void main(String[] args) {
        JavaStreamingContext streamingContext = SingleContext.getInstance().getJavaStreamingContext();
        JavaSparkContext sparkContext = SingleContext.getInstance().getJavaSparkContext();
        JavaInputDStream<ConsumerRecord<String, String>> dStream = SingleContext.getInstance().getJavaInputDStream();

        BroadCastFactory broadCastFactory = new BroadCastFactory(sparkContext);
        Broadcast<RedisPoolSerializable> redisBroadcast = broadCastFactory.getRedisBroadcast();
        Broadcast<MongoClientSerializable> mongoBroadcast = broadCastFactory.getMongoBroadcast();
        Broadcast<KafkaProducerSerializable> kafkaBroadcast = broadCastFactory.getKafkaBroadcast();

        JavaDStream<Signaling> signalingDS = dStream.map(new Function<ConsumerRecord<String, String>, Signaling>() {
            @Override
            public Signaling call(ConsumerRecord<String, String> consumerRecord) {
                return new Gson().fromJson(consumerRecord.value(), Signaling.class);
            }
        });

        HandleSignaling handleSignaling = new HandleSignaling(redisBroadcast, mongoBroadcast, kafkaBroadcast);
        signalingDS.foreachRDD(handleSignaling);

        streamingContext.start();
        try {
            streamingContext.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
