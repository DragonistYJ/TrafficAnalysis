package pool;

/**
 * @ClassName ConnectionPool
 * @Author Dragonist
 * @Date 2020/3/2
 * @Description 连接池接口
 */
public interface ConnectionPool<T> {
    /**
     * 初始化连接池
     *
     * @param maxActive 最大活动连接数
     * @param maxWait   最大等待时间
     */
    void init(int maxActive, long maxWait);

    /**
     * 通过连接池获取连接
     *
     * @return 连接
     */
    T getConnection() throws Exception;


    /**
     * 创建单一的一个连接，不经过连接池
     *
     * @return 单一的连接
     */
    T creatSingleConnection();

    /**
     * 释放连接
     *
     * @param connection 连接对象
     */
    void release(T connection) throws Exception;

    /**
     * 关闭连接池
     */
    void close();
}
