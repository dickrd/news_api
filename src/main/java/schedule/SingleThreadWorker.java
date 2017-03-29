package schedule;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import content.JsoupConfig;
import content.JsoupContentParser;
import content.Record;
import download.BaiduSearch;
import download.NeteaseClient;
import storage.CsvFileStorage;

import java.io.IOException;
import java.nio.charset.Charset;
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
    private NeteaseClient neteaseClient;
    private CsvFileStorage csvFileStorage;

    public SingleThreadWorker() {
        try {
            baiduSearch = new BaiduSearch();
            neteaseClient = new NeteaseClient();
            csvFileStorage = new CsvFileStorage();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error initializing!", e);
        }
    }

    public void feeds(String[] keywords) {
        for (String keyword : keywords) {
            try {
                String searchResult = baiduSearch.searchNews(keyword);
                List<String> links = JsoupContentParser.parseLinks(searchResult, "",
                        JsoupConfig.getSelectors("news.baidu.com"));

                for (String link : links) {
                    if (!link.contains("news.163.com") && !link.contains("money.163.com") && !link.contains("tech.163.com"))
                        continue;

                    String download = baiduSearch.download(link, Charset.forName("gbk"));
                    Record record = JsoupContentParser.parseRecord(download, link, JsoupConfig.getSelectors("news.163.com"));
                    JsonObject jsonObject = new Gson().fromJson(baiduSearch.getNeteaseCount(link), JsonObject.class);
                    int vote = jsonObject.get("cmtVote").getAsInt();
                    int reply = jsonObject.get("rcount").getAsInt();
                    record.setCommentCount(reply);
                    record.setParticipateCount(reply + vote);
                    csvFileStorage.store(record);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Keyword failed.", e);
            }
        }
    }
}
