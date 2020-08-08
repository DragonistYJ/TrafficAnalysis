package pool;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

import java.io.Serializable;

/**
 * @ClassName MongoClientSerializable
 * @Author DragonistYJ
 * @Date 2020/3/9
 * @Description MongoDB连接池，实现序列化接口
 */
public class MongoClientSerializable extends MongoClient implements Serializable {
    public MongoClientSerializable(ServerAddress addr, MongoClientOptions options) {
        super(addr, options);
    }
}
