package love.mmjj.server.repository;

import love.mmjj.server.bean.BaseStation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BaseStationRepository extends MongoRepository<BaseStation, String> {
    BaseStation findByLacAndCell(String lac, String cell);
}
