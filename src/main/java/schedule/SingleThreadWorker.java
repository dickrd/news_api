package schedule;

import content.JsoupConfig;
import content.JsoupContentParser;
import content.Record;
import download.BaiduSearch;
import message.TaskAssignment;
import storage.DatabaseConnection;
import util.SecurityUtil;

import java.util.ArrayList;
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

    private static final String[] protocols = new String[]{"http://", "https://"};

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
            logger.log(Level.INFO, "Initialized.");
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
            logger.log(Level.INFO, "Task added: " + id);
            return id;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to feed.", e);
            return "";
        }
    }

    public static Record[] query(String id) {
        if (!hasInited)
            init();

        Record[] emptyRecords = new Record[0];

        return databaseConnection.get(id).toArray(emptyRecords);
    }

    public static TaskAssignment dispatch(String[] names, int size) {
        if (!hasInited)
            init();

        String aTask = redisConnection.getTask();
        List<String> urls = redisConnection.getUrls(aTask, size);

        List<String> matched = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();
        boolean isMatched;
        for (String url: urls) {
            isMatched = false;
            for (String name: names) {
                for (String protocol: protocols) {
                    if (url.startsWith(protocol + name)) {
                        isMatched = true;
                        break;
                    }
                }
                if (isMatched)
                    break;
            }

            if (isMatched)
                matched.add(url);
            else
                unmatched.add(url);
        }
        redisConnection.addUrls(aTask, unmatched);
        logger.log(Level.INFO, "Task dispatched.");

        return new TaskAssignment(aTask, matched.toArray(new String[0]));
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
                            logger.log(Level.INFO, "Waiting for new task.");
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

                logger.log(Level.INFO, "Search started.");
                for (String keyword : keywords) {
                    try {
                        String searchResult = baiduSearch.searchNews(keyword);
                        List<String> links = JsoupContentParser.parseLinks(searchResult, BaiduSearch.queryUrlBaidu,
                                JsoupConfig.getSelectors("news.baidu.com"));
                        redisConnection.addUrls(id, links);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Keyword failed: " + keyword, e);
                    }
                }
            }
        }
    }
}
