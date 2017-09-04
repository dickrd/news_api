package main.java.schedule;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.content.Record;
import main.java.message.TaskAssignment;
import main.java.source.SearchSource;
import main.java.storage.DatabaseConnection;
import main.java.util.SecurityUtil;

import java.io.FileReader;
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

    private static boolean hasInitialized = false;
    private static Worker worker;
    private static SearchSource searchSource;
    private static RedisConnection redisConnection;
    private static DatabaseConnection databaseConnection;

    private static void init() {
        try {
            worker = new Worker();
            worker.start();

            // TODO separate source.
            SearchSource.SearchEngine[] engines = new Gson().fromJson(new FileReader("source.json"),
                    new TypeToken<SearchSource.SearchEngine[]>(){}.getType());
            searchSource = new SearchSource(engines);
            redisConnection = new RedisConnection();
            databaseConnection = new DatabaseConnection();

            hasInitialized = true;
            logger.log(Level.INFO, "Initialized.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing!", e);
        }
    }

    public static String feeds(String[] keywords) {
        if (!hasInitialized)
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
        if (!hasInitialized)
            init();

        Record[] emptyRecords = new Record[0];

        return databaseConnection.get(id).toArray(emptyRecords);
    }

    public static TaskAssignment dispatch(String[] names, int size) {
        if (!hasInitialized)
            init();

        String aTask = redisConnection.getTask();
        if (aTask == null || aTask.contentEquals("")) {
            logger.log(Level.INFO, "No more task.");
            return new TaskAssignment(aTask, new String[0]);
        }

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
        redisConnection.returnUrls(aTask, unmatched);
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
                        String[] links = searchSource.searchAll(keyword);
                        redisConnection.addUrls(id, links);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Keyword failed: " + keyword, e);
                    }
                }
            }
        }
    }
}
