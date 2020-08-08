package signaling;

import bean.Signaling;
import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class SignalingProducer implements Runnable {
    private List<Signaling> signalingList;
    private Producer<String, String> kafkaProducer;
    private Gson gson;

    public SignalingProducer(List<Signaling> signalingList) {
        this.signalingList = signalingList;
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.kafkaProducer = new KafkaProducer<>(props);
        this.gson = new Gson();
    }

    @Override
    public synchronized void run() {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());
        Iterator<Signaling> iterator = signalingList.iterator();
        while (iterator.hasNext()) {
            Signaling signaling = iterator.next();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(signaling.getTimestamp());

            if (current.get(Calendar.HOUR_OF_DAY) > calendar.get(Calendar.HOUR_OF_DAY)
                    || (current.get(Calendar.HOUR_OF_DAY) >= calendar.get(Calendar.HOUR_OF_DAY) && current.get(Calendar.MINUTE) > calendar.get(Calendar.MINUTE))
                    || (current.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY) && current.get(Calendar.MINUTE) == calendar.get(Calendar.MINUTE) && current.get(Calendar.SECOND) >= calendar.get(Calendar.SECOND))) {
                System.out.println(signaling);
                kafkaProducer.send(new ProducerRecord<>("heatmap.newdata", "signaling", gson.toJson(signaling)));
                iterator.remove();
            }
        }
    }


    public List<Signaling> getSignalingList() {
        return signalingList;
    }

    public void setSignalingList(List<Signaling> signalingList) {
        this.signalingList = signalingList;
    }
}
