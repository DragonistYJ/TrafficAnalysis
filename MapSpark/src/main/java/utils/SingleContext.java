package utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName SingleContext
 * @Author Dragonist
 * @Date 2020/3/2
 * @Description 创建SparkContext的单例类，单线程所以没有做锁
 */
public class SingleContext {
    private SparkSession sparkSession;
    private JavaSparkContext javaSparkContext;
    private JavaStreamingContext javaStreamingContext;
    private JavaInputDStream<ConsumerRecord<String, String>> javaInputDStream;

    private SingleContext() {
        this.sparkSession = SparkSession.builder()
                .master("local[*]")
                .appName("Map")
                .config("spark.mongodb.input.uri", "mongodb://mongodb:27016/traffic-analysis.BaseStation")
                .config("spark.mongodb.output.uri", "mongodb://mongodb:27016/traffic-analysis.StationHourReport")
                .getOrCreate();

        this.javaSparkContext = new JavaSparkContext(sparkSession.sparkContext());
        this.javaStreamingContext = new JavaStreamingContext(this.javaSparkContext, Durations.seconds(1));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "kafka:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "traffic-analysis");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);
        Collection<String> topics = Collections.singletonList("heatmap.newdata");
        this.javaInputDStream = KafkaUtils.createDirectStream(
                this.javaStreamingContext,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaParams));
    }

    private static SingleContext singleContext;

    public static synchronized SingleContext getInstance() {
        if (singleContext == null) {
            singleContext = new SingleContext();
        }
        return singleContext;
    }

    public JavaSparkContext getJavaSparkContext() {
        return javaSparkContext;
    }

    public JavaStreamingContext getJavaStreamingContext() {
        return javaStreamingContext;
    }

    public JavaInputDStream<ConsumerRecord<String, String>> getJavaInputDStream() {
        return javaInputDStream;
    }
}
