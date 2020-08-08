package love.mmjj.server.websocket;

import love.mmjj.server.pool.KafkaConsumerPool;
import love.mmjj.server.thread.ConsumeHeatMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/map/heatmap")
public class HeatMapSocket {
    private static AtomicInteger connectSize = new AtomicInteger(0);
    private static AtomicBoolean isSocketOpen = new AtomicBoolean(false);
    private static KafkaConsumerPool kafkaConsumerPool;
    public static ConcurrentLinkedQueue<HeatMapSocket> heatMapSocketQueue = new ConcurrentLinkedQueue<>();
    public static ScheduledExecutorService scheduledExecutorService;

    private Session session;
    private Logger logger = LoggerFactory.getLogger(HeatMapSocket.class);

    /**
     * 连接成功时触发该方法，记录session
     *
     * @param session 连接会话
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        heatMapSocketQueue.offer(this);
        logger.info("有新的连接加入，当前连接数为" + connectSize.incrementAndGet());

        if (isSocketOpen.compareAndSet(false, true)) {
            scheduledExecutorService = Executors.newScheduledThreadPool(5);
            kafkaConsumerPool = new KafkaConsumerPool(5, 5000, "Kafka", Collections.singletonList("heatmap.stationPeopleNumber"));
            scheduledExecutorService.scheduleWithFixedDelay(new ConsumeHeatMap(kafkaConsumerPool), 0, 1, TimeUnit.SECONDS);
            logger.info("开始订阅heatmap.stationPeopleNumber");
        }
    }

    /**
     * 关闭连接时触发
     */
    @OnClose
    public void onClose() {
        heatMapSocketQueue.remove(this);
        if (connectSize.decrementAndGet() == 0 && isSocketOpen.compareAndSet(true, false)) {
            scheduledExecutorService.shutdown();
            kafkaConsumerPool.close();
            logger.info("停止订阅heatmap.peopleNumber");
        }
        logger.info("一条连接断开，剩余连接数为" + connectSize.get());
    }

    /**
     * 发生错误时触发，移除该连接
     *
     * @param error 错误内容
     * @throws IOException 异常
     */
    @OnError
    public void onError(Throwable error) throws IOException {
        error.printStackTrace();
        this.session.close();
        heatMapSocketQueue.remove(this);
        logger.error("连接发生异常");
    }

    public Session getSession() {
        return session;
    }
}
