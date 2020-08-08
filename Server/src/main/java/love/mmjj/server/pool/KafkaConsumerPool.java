package love.mmjj.server.pool;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;
import java.util.Properties;

public class KafkaConsumerPool extends SimpleConnectionPool<KafkaConsumer<String, String>> {
    private List<String> topics;

    public KafkaConsumerPool(int maxActive, long maxWait, String poolName, List<String> topics) {
        super(maxActive, maxWait, poolName);
        this.topics = topics;
    }

    @Override
    public synchronized void close() {
        if (isClosed.compareAndSet(false, true)) {
            for (KafkaConsumer<String, String> consumer : freeQueue) {
                consumer.close();
            }
            for (KafkaConsumer<String, String> consumer : busyQueue) {
                consumer.close();
            }
        }
    }

    @Override
    public KafkaConsumer<String, String> creatSingleConnection() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("group.id", "server");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(this.topics);
        return consumer;
    }
}
