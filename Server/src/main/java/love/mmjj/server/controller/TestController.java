package love.mmjj.server.controller;

import love.mmjj.server.bean.BaseStation;
import love.mmjj.server.pool.util.JedisPoolUtil;
import love.mmjj.server.repository.BaseStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

@RestController
public class TestController {
    @Autowired
    private BaseStationRepository baseStationRepository;

    @ResponseBody
    @GetMapping("/test")
    public String test() {
        BaseStation baseStation = baseStationRepository.findByLacAndCell("16797", "2584075");
        System.out.println(baseStation.get_id());
        return baseStation.toString();
    }
}
