package love.mmjj.server.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SimpleConnectionPool<T> implements ConnectionPool<T> {
    protected int maxActive;
    protected long maxWait;
    protected String poolName;
    // 空闲队列
    protected LinkedBlockingQueue<T> freeQueue = new LinkedBlockingQueue<>();
    // 繁忙队列
    protected LinkedBlockingQueue<T> busyQueue = new LinkedBlockingQueue<>();
    // 连接池活动连接数
    protected AtomicInteger activeSize = new AtomicInteger(0);
    // 连接池总共连接数
    protected AtomicInteger createSize = new AtomicInteger(0);
    // 连接池关闭标记
    protected AtomicBoolean isClosed = new AtomicBoolean(false);
    protected Logger logger;

    public SimpleConnectionPool(int maxActive, long maxWait, String poolName) {
        this.poolName = poolName;
        this.init(maxActive, maxWait);
        logger = LoggerFactory.getLogger(poolName);
    }

    @Override
    public void init(int maxActive, long maxWait) {
        this.maxActive = maxActive <= 0 ? Integer.MAX_VALUE : maxActive;
        this.maxWait = maxWait;
    }

    @Override
    public synchronized T getConnection() throws Exception {
        T t;
        long currentTime = System.currentTimeMillis();

        // 空闲队列无连接
        if ((t = freeQueue.poll()) == null) {
            // 线程数小于最大线程数
            if (activeSize.get() < maxActive) {
                // 先增加池中连接数后判断是否小于等于maxActive
                if (activeSize.incrementAndGet() <= maxActive) {
                    t = this.creatSingleConnection();
                    logger.info("Thread(" + Thread.currentThread().getId() + ")获取连接" + this.poolName + createSize.incrementAndGet() + "条");
                    if (busyQueue == null) System.out.println("队列未初始化");
                    if (t == null) System.out.println(this.poolName + "  空的");
                    busyQueue.add(t);
                    return t;
                } else {
                    System.out.println(4);
                    // 如增加后发现大于maxActive则减去增加的
                    activeSize.decrementAndGet();
                }
            }

            logger.info("Thread(" + Thread.currentThread().getId() + ")等待" + this.poolName + "获取空闲资源");
            long waitTime = maxWait - (System.currentTimeMillis() - currentTime);
            try {
                t = freeQueue.poll(waitTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new Exception("等待获取" + this.poolName + "连接异常");
            }

            if (t != null) {
                logger.info("Thread(" + Thread.currentThread().getId() + ")获取连接" + this.poolName + createSize.incrementAndGet() + "条");
                busyQueue.offer(t);
                return t;
            } else {
                logger.error("Thread(" + Thread.currentThread().getId() + ")获取连接" + this.poolName + "超时,请重试!");
                throw new Exception("Thread(" + Thread.currentThread().getId() + ")获取连接" + this.poolName + "超时,请重试!");
            }
        }

        logger.info("Thread(" + Thread.currentThread().getId() + ")从" + this.poolName + "空闲队列中获取连接");
        busyQueue.offer(t);
        return t;
    }

    @Override
    public synchronized void release(T connection) {
        if (connection == null) {
            logger.error("释放的" + this.poolName + "连接为空");
            return;
        }
        if (busyQueue.remove(connection)) {
            freeQueue.offer(connection);
            logger.info("成功释放" + this.poolName + "连接");
        } else {
            activeSize.decrementAndGet();
            logger.error(this.poolName + "释放失败");
        }
    }

    @Override
    public abstract void close();

    @Override
    public abstract T creatSingleConnection();
}
