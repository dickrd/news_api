package schedule;

import content.JsoupConfig;
import content.JsoupContentParser;
import content.Record;
import download.BaiduSearch;
import message.TaskAssignment;
import storage.DatabaseConnection;
import util.SecurityUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/28/2017.
 * A worker that performs download and parse task in a single thread.
 */
public class SingleThreadWorker {

    private static final Logger logger = Logger.getLogger(SingleThreadWorker.class.getName());

    private static boolean hasInited = false;
    private static Worker worker;
    private static BaiduSearch baiduSearch;
    private static RedisConnection redisConnection;
    private static DatabaseConnection databaseConnection;

    private static void init() {
        try {
            worker = new Worker();
            baiduSearch = new BaiduSearch();
            redisConnection = new RedisConnection();
            databaseConnection = new DatabaseConnection();

            hasInited = true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing!", e);
        }
    }

    public static String feeds(String[] keywords) {
        if (!hasInited)
            init();

        try {
            String id = SecurityUtil.bytesToHex(SecurityUtil.md5(Arrays.toString(keywords).getBytes()));
            worker.newTask(id, keywords);
            return id;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to feed.", e);
            return "";
        }
    }

    public static Record[] query(String id) {
        Record[] emptyRecords = new Record[0];
        if (!hasInited)
            return emptyRecords;

        return databaseConnection.get(id).toArray(emptyRecords);
    }

    public static TaskAssignment dispatch(String[] names, int size) {
        return null;
    }

    private static class Worker extends Thread {

        private static final Logger logger = Logger.getLogger(Worker.class.getName());

        final HashMap<String, String[]> workMap = new HashMap<>();

        void newTask(String id, String keywords[]) {
            synchronized (workMap) {
                workMap.put(id, keywords);
                workMap.notify();
            }
        }

        @Override
        public void run() {
            String id;
            String keywords[];

            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (workMap) {
                    while (workMap.isEmpty()) {
                        try {
                            workMap.wait();
                        } catch (InterruptedException e) {
                            logger.log(Level.WARNING, "Error.", e);
                        }
                    }

                    try {
                        id = workMap.keySet().iterator().next();
                        keywords = workMap.remove(id);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "No keywords.", e);
                        continue;
                    }
                }

                for (String keyword : keywords) {
                    try {
                        String searchResult = baiduSearch.searchNews(keyword);
                        List<String> links = JsoupContentParser.parseLinks(searchResult, "",
                                JsoupConfig.getSelectors("news.baidu.com"));
                        redisConnection.add(id, links);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Keyword failed: " + keyword, e);
                    }
                }
            }
        }
    }
}
