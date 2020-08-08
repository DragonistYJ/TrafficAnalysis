package functions;

import bean.BaseStation;
import bean.Migration;
import bean.Signaling;
import bean.StationPeopleNumber;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pool.KafkaProducerSerializable;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @ClassName SignalingHandler
 * @Author DragonistYJ
 * @Date 2020/3/9
 * @Description 处理信令数据
 */
public class SignalingHandler {
    private Jedis jedis;
    private MongoCollection<Document> mongoCollection;
    private KafkaProducerSerializable kafkaProducer;
    private Logger logger;
    private Gson gson;

    public SignalingHandler(Jedis jedis, MongoCollection<Document> mongoCollection, KafkaProducerSerializable kafkaProducer) {
        this.jedis = jedis;
        this.mongoCollection = mongoCollection;
        this.kafkaProducer = kafkaProducer;
        this.logger = LoggerFactory.getLogger(SignalingHandler.class);
        this.gson = new Gson();
    }

    /**
     * 人第N次出现 基站第N次记录
     * latestSignaling -> {imsi} 人的位置
     * peopleNumber -> {stationID} 前基站人数 -1 现基站人数 +1
     * {timeTag}.peopleNumber -> {stationID} 该时间段基站最后的人数
     * {timeTag}.peopleNumberList 前基站 rpush -1 现基站 rpush +1
     * migration.{timeTag} 迁徙链表 rpush
     * heatmap.stationPeopleNumber 基站人数及信息 send
     *
     * @param signaling 信令数据
     */
    public void peopleSeveralStationSeveral(Signaling signaling) {
        String currentStationID = signaling.getLac() + "-" + signaling.getCell();
        BaseStation currentStation = getBaseStation(signaling.getLac(), signaling.getCell());
        if (currentStation == null) return;

        Signaling previousSignaling = gson.fromJson(jedis.hget("latestSignaling", signaling.getImsi()), Signaling.class);
        String previousStationID = previousSignaling.getLac() + "-" + previousSignaling.getCell();
        BaseStation previousStation = getBaseStation(previousSignaling.getLac(), previousSignaling.getCell());
        if (previousStation == null) return;

        jedis.hset("latestSignaling", signaling.getImsi(), gson.toJson(signaling));
        // 位置没有变，不需要更新数据
        if (currentStationID.equals(previousStationID)) return;

        jedis.hincrBy("peopleNumber", currentStationID, 1);
        jedis.hincrBy(getTimeTag(signaling.getTimestamp()) + ".peopleNumber", currentStationID, 1);
        jedis.hincrBy("peopleNumber", previousStationID, -1);
        jedis.hincrBy(getTimeTag(previousSignaling.getTimestamp()) + ".peopleNumber", previousStationID, -1);

        StationPeopleNumber current = new StationPeopleNumber().setByBaseStation(currentStation);
        current.setPeopleNumber(Integer.valueOf(jedis.hget("peopleNumber", currentStationID)));
        jedis.rpush(getTimeTag(signaling.getTimestamp()) + ".peopleNumberList", gson.toJson(current));
        StationPeopleNumber previous = new StationPeopleNumber().setByBaseStation(previousStation);
        previous.setPeopleNumber(Integer.valueOf(jedis.hget("peopleNumber", previousStationID)));
        jedis.rpush(getTimeTag(signaling.getTimestamp()) + ".peopleNumberList", gson.toJson(previous));

        // 为了缩小数据体积
        currentStation.slim();
        previousStation.slim();
        Migration migration = new Migration(signaling.getTimestamp(), signaling.getImsi(), previousStation, currentStation);
        jedis.rpush(getTimeTag(signaling.getTimestamp()) + ".migration", gson.toJson(migration));

        kafkaProducer.send(new ProducerRecord<>("heatmap.stationPeopleNumber", gson.toJson(current)));
        kafkaProducer.send(new ProducerRecord<>("heatmap.stationPeopleNumber", gson.toJson(previous)));
        logger.info("人第N次基站第N次 " + signaling.toString());
    }

    /**
     * 人第N次出现 基站第1次记录
     * latestSignaling -> {imsi} 人的位置
     * peopleNumber -> {stationID} 前基站人数 -1 现基站人数 1
     * {timeTag}.peopleNumber -> {stationID} 该时间段基站最后的人数
     * {timeTag}.peopleNumberList 前基站 rpush -1 现基站 rpush 1
     * migration.{timeTag} 迁徙链表 rpush
     * heatmap.stationPeopleNumber 基站人数及信息 send
     *
     * @param signaling 信令数据
     */
    public void peopleSeveralStationFirst(Signaling signaling) {
        String currentStationID = signaling.getLac() + "-" + signaling.getCell();
        BaseStation currentStation = getBaseStation(signaling.getLac(), signaling.getCell());
        if (currentStation == null) return;

        Signaling previousSignaling = gson.fromJson(jedis.hget("latestSignaling", signaling.getImsi()), Signaling.class);
        String previousStationID = previousSignaling.getLac() + "-" + previousSignaling.getCell();
        BaseStation previousStation = getBaseStation(previousSignaling.getLac(), previousSignaling.getCell());
        if (previousStation == null) return;

        jedis.hset("latestSignaling", signaling.getImsi(), gson.toJson(signaling));

        jedis.hincrBy("peopleNumber", currentStationID, 1);
        jedis.hincrBy(getTimeTag(signaling.getTimestamp()) + ".peopleNumber", currentStationID, 1);
        jedis.hincrBy("peopleNumber", previousStationID, -1);
        jedis.hincrBy(getTimeTag(previousSignaling.getTimestamp()) + ".peopleNumber", previousStationID, -1);
        jedis.hset("firstTime", currentStationID, String.valueOf(signaling.getTimestamp()));

        StationPeopleNumber current = new StationPeopleNumber().setByBaseStation(currentStation);
        current.setPeopleNumber(Integer.valueOf(jedis.hget("peopleNumber", currentStationID)));
        jedis.rpush(getTimeTag(signaling.getTimestamp()) + ".peopleNumberList", gson.toJson(current));

        StationPeopleNumber previous = new StationPeopleNumber().setByBaseStation(previousStation);
        previous.setPeopleNumber(Integer.valueOf(jedis.hget("peopleNumber", previousStationID)));
        jedis.rpush(getTimeTag(signaling.getTimestamp()) + ".peopleNumberList", gson.toJson(previous));

        // 为了缩小数据体积
        currentStation.slim();
        previousStation.slim();
        Migration migration = new Migration(signaling.getTimestamp(), signaling.getImsi(), previousStation, currentStation);
        jedis.rpush(getTimeTag(signaling.getTimestamp()) + ".migration", gson.toJson(migration));

        kafkaProducer.send(new ProducerRecord<>("heatmap.stationPeopleNumber", gson.toJson(current)));
        kafkaProducer.send(new ProducerRecord<>("heatmap.stationPeopleNumber", gson.toJson(previous)));
        logger.info("人第N次基站第1次 " + signaling.toString());
    }

    /**
     * 人第1次出现 基站第N次记录
     * latestSignaling -> {imsi} 人的位置
     * peopleNumber -> {stationID} 基站人数 +1
     * {timeTag}.peopleNumber -> {stationID} 该时间段基站最后的人数
     * {timeTag}.peopleNumberList 基站小时人数链表 rpush +1后的人数
     * heatmap.stationPeopleNumber 基站人数及信息 send
     *
     * @param signaling 信令数据
     */
    public void peopleFirstStationSeveral(Signaling signaling) {
        String stationID = signaling.getLac() + "-" + signaling.getCell();
        BaseStation currentStation = getBaseStation(signaling.getLac(), signaling.getCell());
        if (currentStation == null) return;

        jedis.hset("latestSignaling", signaling.getImsi(), gson.toJson(signaling));

        jedis.hincrBy("peopleNumber", stationID, 1);
        jedis.hincrBy(getTimeTag(signaling.getTimestamp()) + ".peopleNumber", stationID, 1);

        StationPeopleNumber stationPeopleNumber = new StationPeopleNumber().setByBaseStation(currentStation);
        stationPeopleNumber.setPeopleNumber(Integer.valueOf(jedis.hget("peopleNumber", stationID)));
        jedis.rpush(getTimeTag(signaling.getTimestamp()) + ".peopleNumberList", gson.toJson(stationPeopleNumber));

        kafkaProducer.send(new ProducerRecord<>("heatmap.stationPeopleNumber", gson.toJson(stationPeopleNumber)));
        logger.info("人第1次基站第N次 " + signaling.toString());
    }

    /**
     * 人第1次出现 基站第1次记录
     * latestSignaling -> {imsi} 人的位置
     * peopleNumber -> {stationID} 基站人数 set 1
     * {timeTag}.peopleNumber -> {stationID} 该时间段基站最后的人数
     * {timeTag}.peopleNumberList 基站小时人数链表 rpush 1
     * heatmap.stationPeopleNumber 基站人数及信息 send
     *
     * @param signaling 信令数据
     */
    public void peopleFirstStationFirst(Signaling signaling) {
        String stationID = signaling.getLac() + "-" + signaling.getCell();
        BaseStation currentStation = getBaseStation(signaling.getLac(), signaling.getCell());
        if (currentStation == null) return;

        jedis.hset("latestSignaling", signaling.getImsi(), gson.toJson(signaling));

        jedis.hincrBy("peopleNumber", stationID, 1);
        jedis.hincrBy(getTimeTag(signaling.getTimestamp()) + ".peopleNumber", stationID, 1);
        jedis.hset("firstTime", stationID, String.valueOf(signaling.getTimestamp()));

        StationPeopleNumber stationPeopleNumber = new StationPeopleNumber().setByBaseStation(currentStation);
        stationPeopleNumber.setPeopleNumber(Integer.valueOf(jedis.hget("peopleNumber", stationID)));
        jedis.rpush(getTimeTag(signaling.getTimestamp()) + ".peopleNumberList", gson.toJson(stationPeopleNumber));

        kafkaProducer.send(new ProducerRecord<>("heatmap.stationPeopleNumber", gson.toJson(stationPeopleNumber)));
        logger.info("人第1次基站第1次 " + signaling.toString());
    }

    /**
     * 格式化时间戳为时间标记
     *
     * @param timestamp 时间戳
     * @return 格式化后的时间
     */
    private String getTimeTag(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd-HH");
        return dateFormat.format(calendar.getTime());
    }

    /**
     * 从MongoDB或Redis获取基站信息，二级缓存
     *
     * @param lac  基站识别符
     * @param cell 基站识别符
     * @return 基站实体类
     */
    private BaseStation getBaseStation(String lac, String cell) {
        String stationID = lac + "-" + cell;
        JsonWriterSettings settings = JsonWriterSettings.builder().objectIdConverter((objectId, writer) -> writer.writeString(objectId.toString())).build();

        if (!jedis.hexists("baseStation", stationID)) {
            // 根据lac和cell进行搜索
            BasicDBObject condition = new BasicDBObject();
            condition.put("lac", lac);
            condition.put("cell", cell);
            FindIterable<Document> documents = mongoCollection.find(condition);
            if (!documents.iterator().hasNext()) return null;

            String stationJson = documents.iterator().next().toJson(settings);
            jedis.hset("baseStation", stationID, stationJson);
            logger.info("基站信息缓存入Redis：" + stationJson);
            return gson.fromJson(stationJson, BaseStation.class);
        } else {
            return gson.fromJson(jedis.hget("baseStation", stationID), BaseStation.class);
        }
    }
}
