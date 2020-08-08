package pool;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.io.Serializable;
import java.util.Properties;

/**
 * @ClassName KafkaProducerSerializable
 * @Author DragonistYJ
 * @Date 2020/3/9
 * @Description kafka连接池，实现序列化
 */
public class KafkaProducerSerializable extends KafkaProducer<String, String> implements Serializable {
    public KafkaProducerSerializable(Properties properties) {
        super(properties);
    }
}
