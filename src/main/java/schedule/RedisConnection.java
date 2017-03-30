package schedule;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/29/2017.
 * Store url to Redis.
 */
class RedisConnection {

    private static final Logger logger = Logger.getLogger(RedisConnection.class.getName());
    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

    private static final String taskSet = "tasks";
    private static final String taskList = "queue";
    private static final String urlSetPrefix = "task:";
    private static final String urlListPrefix = "task:queue:";

    void addUrls(String id, List<String> urls) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.sismember(taskSet, id)) {
                jedis.sadd(taskSet, id);
                jedis.lpush(taskList, id);
            }

            for (String url: urls) {
                Boolean isMember = jedis.sismember(urlSetPrefix + id, url);
                if (!isMember) {
                    jedis.sadd(urlSetPrefix + id, url);
                    jedis.lpush(urlListPrefix + id, url);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Insert failed.", e);
        }
    }

    String getTask() {
        try (Jedis jedis = pool.getResource()) {
            return jedis.rpop(taskList);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Get failed.", e);
            return "";
        }
    }

    List<String> getUrls(String id, int size) {
        List<String> urls = new ArrayList<>();

        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < size; i++) {
                urls.add(jedis.rpop(urlListPrefix + id));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Get failed.", e);
        }

        return urls;
    }
}
