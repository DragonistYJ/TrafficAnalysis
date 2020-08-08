package love.mmjj.server.thread;

import com.google.gson.Gson;
import love.mmjj.server.bean.StationPeopleNumber;
import love.mmjj.server.pool.KafkaConsumerPool;
import love.mmjj.server.websocket.HeatMapSocket;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;

public class ConsumeHeatMap implements Runnable {
    private KafkaConsumerPool kafkaConsumerPool;
    private Logger logger = LoggerFactory.getLogger(ConsumeHeatMap.class);

    public ConsumeHeatMap(KafkaConsumerPool kafkaConsumerPool) {
        this.kafkaConsumerPool = kafkaConsumerPool;
    }

    @Override
    public synchronized void run() {
        ConsumerRecords<String, String> records = null;
        KafkaConsumer<String, String> kafkaConsumer = null;
        Gson gson = new Gson();
        try {
            kafkaConsumer = this.kafkaConsumerPool.getConnection();
            records = kafkaConsumer.poll(Duration.ofSeconds(1));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Kafka连接池获取失败");
        }

        if (records == null) return;
        HashSet<StationPeopleNumber> stationPeopleNumbers = new HashSet<>();
        for (ConsumerRecord<String, String> record : records) {
            StationPeopleNumber stationPeopleNumber = gson.fromJson(record.value(), StationPeopleNumber.class);
            if (stationPeopleNumbers.contains(stationPeopleNumber)) continue;
            stationPeopleNumbers.add(stationPeopleNumber);
        }

        for (StationPeopleNumber stationPeopleNumber : stationPeopleNumbers) {
            logger.info("发布新的地点人数:" + stationPeopleNumber.toString());
            for (HeatMapSocket heatMapSocket : HeatMapSocket.heatMapSocketQueue) {
                try {
                    heatMapSocket.getSession().getBasicRemote().sendText(gson.toJson(stationPeopleNumber));
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("Session:" + heatMapSocket.getSession().getId() + "发送消息失败");
                }
            }
        }

        this.kafkaConsumerPool.release(kafkaConsumer);
    }
}
