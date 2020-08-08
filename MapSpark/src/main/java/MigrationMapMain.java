import bean.*;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.spark.MongoSpark;
import functions.*;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.bson.Document;
import pool.RedisPool;
import redis.clients.jedis.Jedis;
import scala.Tuple2;
import utils.SingleContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName MigrationMapMain
 * @Author DragonistYJ
 * @Date 2020/3/3
 * @Description 迁徙图主类
 */
public class MigrationMapMain {
    public static void main(String[] args) {
        JavaSparkContext javaSparkContext = SingleContext.getInstance().getJavaSparkContext();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(2018, Calendar.OCTOBER, 3);
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd-HH");
        String timeTag = dateFormat.format(calendar.getTime());

        Broadcast<RedisPool> redisPoolBroadcast = javaSparkContext.broadcast(new RedisPool(5, 5000, "Redis"));
        Jedis jedis = new Jedis("redis", 6379);

        // 人数最多最少
        List<String> peopleNumberList = jedis.lrange(timeTag + ".peopleNumberList", 0, -1);

        JavaPairRDD<String, StationPeopleNumber> peopleNumberRDD = javaSparkContext.parallelize(peopleNumberList).mapToPair(new PairFunction<String, String, StationPeopleNumber>() {
            @Override
            public Tuple2<String, StationPeopleNumber> call(String s) {
                StationPeopleNumber stationPeopleNumber = new Gson().fromJson(s, StationPeopleNumber.class);
                return new Tuple2<>(stationPeopleNumber.getLac() + "-" + stationPeopleNumber.getCell(), stationPeopleNumber);
            }
        });

        // 保存时段中基站最大人数
        ComparePeopleNumber comparePeopleNumber = new ComparePeopleNumber(true);
        JavaPairRDD<String, StationPeopleNumber> maxPeopleNumber = peopleNumberRDD.reduceByKey(comparePeopleNumber);
        SaveNumberMaxMin saveNumberMaxMin = new SaveNumberMaxMin(".max", redisPoolBroadcast);
        maxPeopleNumber.foreach(saveNumberMaxMin);

        // 保存时段中基站最少人数
        comparePeopleNumber.setLarger(false);
        JavaPairRDD<String, StationPeopleNumber> minPeopleNumber = peopleNumberRDD.reduceByKey(comparePeopleNumber);
        saveNumberMaxMin.setSuffix(".min");
        minPeopleNumber.foreach(saveNumberMaxMin);

        // 迁徙类映射
        List<String> migrationList = jedis.lrange(timeTag + ".migration", 0, -1);
        JavaRDD<Migration> migrationRDD = javaSparkContext.parallelize(migrationList).map(new Function<String, Migration>() {
            @Override
            public Migration call(String s) throws Exception {
                return new Gson().fromJson(s, Migration.class);
            }
        });
        // 保存基站迁入迁出信息
        SaveStationMigration saveStationMigration = new SaveStationMigration(redisPoolBroadcast);
        migrationRDD.foreachPartition(saveStationMigration);

        JavaRDD<BaseStation> stationRDD = MongoSpark.load(javaSparkContext).toDS(BaseStation.class).toJavaRDD().filter(new FilterActiveStation(redisPoolBroadcast));
        SaveHourReport saveHourReport = new SaveHourReport(redisPoolBroadcast, calendar);
        stationRDD.foreachPartition(saveHourReport);

        Gson gson = new Gson();
        List<String> stringList = jedis.lrange(timeTag + ".hourReport", 0, -1);
        AllStationsHourReport report = new AllStationsHourReport();
        report.set_id(timeTag);
        report.setHourReports(new ArrayList<>());
        for (String s : stringList) {
            report.getHourReports().add(gson.fromJson(s, StationHourReport.class));
        }
        MongoCollection<Document> collection = new MongoClient("mongodb", 27016).getDatabase("traffic-analysis").getCollection("StationHourReport");
        Document document = Document.parse(gson.toJson(report));
        collection.insertOne(document);
    }
}
