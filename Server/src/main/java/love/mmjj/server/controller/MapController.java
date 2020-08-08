package love.mmjj.server.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import love.mmjj.server.bean.AllStationsHourReport;
import love.mmjj.server.bean.BaseStation;
import love.mmjj.server.bean.StationHourReport;
import love.mmjj.server.bean.StationPeopleNumber;
import love.mmjj.server.pool.util.JedisPoolUtil;
import love.mmjj.server.repository.BaseStationRepository;
import love.mmjj.server.repository.StationHourReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController()
@RequestMapping("/map")
public class MapController {
    @Autowired
    private BaseStationRepository baseStationRepository;
    @Autowired
    private StationHourReportRepository stationHourReportRepository;

    @PostMapping("/migrationMap/hourStatistics")
    public String minMaxPeopleNumber(@RequestParam String fromTimeTag, @RequestParam String toTimeTag) {
        Calendar fromCalendar = Calendar.getInstance();
        Calendar toCalendar = Calendar.getInstance();
        String[] strings = fromTimeTag.split("-");
        int[] times = new int[4];
        for (int i = 0; i < strings.length; i++) {
            times[i] = Integer.parseInt(strings[i]);
        }
        fromCalendar.set(times[0], times[1] - 1, times[2], times[3], 0, 0);
        strings = toTimeTag.split("-");
        for (int i = 0; i < strings.length; i++) {
            times[i] = Integer.parseInt(strings[i]);
        }
        toCalendar.set(times[0], times[1] - 1, times[2], times[3], 0, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd-HH");
        HashMap<String, StationHourReport> hashMap = new HashMap<>();
        while (fromCalendar.getTimeInMillis() <= toCalendar.getTimeInMillis()) {
            String format = dateFormat.format(fromCalendar.getTime());
            List<StationHourReport> hourReports = stationHourReportRepository.findBy_id(format).getHourReports();
            for (StationHourReport hourReport : hourReports) {
                String stationID = hourReport.getBaseStation().getLac() + "-" + hourReport.getBaseStation().getCell();
                if (hashMap.containsKey(stationID)) {
                    StationHourReport report = hashMap.get(stationID);
                    report.setMaxPeopleNumber(Math.max(report.getMaxPeopleNumber(), hourReport.getMaxPeopleNumber()));
                    report.setMinPeopleNumber(Math.min(report.getMinPeopleNumber(), hourReport.getMinPeopleNumber()));
                    report.getInflow().addAll(hourReport.getInflow());
                    report.getOutflow().addAll(hourReport.getOutflow());
                } else {
                    hashMap.put(stationID, hourReport);
                }
            }
            fromCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        List<StationHourReport> list = new ArrayList<>();
        Set<String> keySet = hashMap.keySet();
        for (String key : keySet) {
            list.add(hashMap.get(key));
        }
        return new Gson().toJson(list);
    }

    /**
     * 从redis里获取缓存着的基站人数
     * {stationID}.peopleNumber 当前基站人数
     *
     * @return JSON格式列表
     */
    @PostMapping("/heatmap/cachedStationPeopleNumber")
    public String cachedStationPeopleNumbers() {
        List<BaseStation> baseStations = baseStationRepository.findAll();
        Jedis jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
        List<StationPeopleNumber> stationPeopleNumbers = new ArrayList<>();
        for (BaseStation baseStation : baseStations) {
            StationPeopleNumber stationPeopleNumber = new StationPeopleNumber().setByBaseStation(baseStation);
            String stationID = baseStation.getLac() + "-" + baseStation.getCell() + ".peopleNumber";
            if (jedis.exists(stationID)) {
                stationPeopleNumber.setPeopleNumber(Integer.valueOf(jedis.get(stationID)));
                stationPeopleNumbers.add(stationPeopleNumber);
            }
        }
        jedis.close();
        return new Gson().toJson(stationPeopleNumbers);
    }
}
