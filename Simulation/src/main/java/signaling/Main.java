package signaling;

import bean.Signaling;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        List<Signaling> signalingList = new ReadRowData().read("/rowdata.csv");
        signalingList.sort(Comparator.comparingLong(Signaling::getTimestamp));
        ScheduledExecutorService producer = Executors.newScheduledThreadPool(1);
        producer.scheduleAtFixedRate(new SignalingProducer(signalingList), 0, 500, TimeUnit.MILLISECONDS);
    }
}
