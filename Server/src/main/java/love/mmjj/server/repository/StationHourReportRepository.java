package love.mmjj.server.repository;

import love.mmjj.server.bean.AllStationsHourReport;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @ClassName StationHourReportRepository
 * @Author DragonistYJ
 * @Date 2020/3/8
 * @Description 小时报表
 */
public interface StationHourReportRepository extends MongoRepository<AllStationsHourReport, String> {
    AllStationsHourReport findBy_id(String _id);
}
