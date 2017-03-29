package schedule;

import content.JsoupConfig;
import content.JsoupContentParser;
import download.BaiduSearch;
import util.SecurityUtil;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/28/2017.
 * A worker that performs download and parse task in a single thread.
 */
public class SingleThreadWorker {

    private static final Logger logger = Logger.getLogger(SingleThreadWorker.class.getName());

    private BaiduSearch baiduSearch;
    private RedisConnection redisConnection;

    public SingleThreadWorker() {
        try {
            baiduSearch = new BaiduSearch();
            redisConnection = new RedisConnection();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing!", e);
        }
    }

    public String feeds(String[] keywords) {
        String id;
        try {
            id = SecurityUtil.bytesToHex(SecurityUtil.md5(Arrays.toString(keywords).getBytes()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to generate id.", e);
            return "";
        }

        for (String keyword : keywords) {
            try {
                String searchResult = baiduSearch.searchNews(keyword);
                List<String> links = JsoupContentParser.parseLinks(searchResult, "",
                        JsoupConfig.getSelectors("news.baidu.com"));
                redisConnection.add(id, links);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Keyword failed.", e);
            }
        }

        return id;
    }
}
