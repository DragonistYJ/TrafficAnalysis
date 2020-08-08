package love.mmjj.server.config;

import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDBConfig {
    @Bean
    public MongoClientOptions mongoClientOptions() {
        return MongoClientOptions.builder().maxConnectionIdleTime(6000).maxConnectionIdleTime(0).build();
    }
}
