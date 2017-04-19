package uestc.news.nlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hehehey.ghost.message.Response;
import com.hehehey.ghost.record.PageData;
import com.hehehey.ghost.util.HttpClient;
import uestc.news.nlp.model.Hanlp;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 4/18/2017.
 *
 */
public class NlpWorker {

    private static final Logger logger = Logger.getLogger(NlpWorker.class.getName());

    private static final String baseUrl = "http://127.0.0.1:666/";
    private static final String dataUrl = "data/%s?page=%d&size=%d";
    private static final String dataSubmitUrl = "data/%s";

    public static final int LENGTH_SUMMARY = 50;
    public static final int SIZE_KEYWORDS = 5;

    private Hanlp hanlp;
    private Gson gson;
    private HttpClient client;

    private NlpWorker() {
        hanlp = new Hanlp(LENGTH_SUMMARY, SIZE_KEYWORDS);
        gson = new Gson();
        client = new HttpClient();
    }

    public static void main(String[] args) throws IOException {
        Logger.getGlobal().addHandler(new ConsoleHandler());
        new NlpWorker().work("i15b6109df77", 0, 5);
    }

    private void work(String taskId, int page, int size) throws IOException {
        PageData[] dataArray = getData(baseUrl + String.format(dataUrl, taskId, page, size));
        for (PageData data : dataArray) {
            try {
                String content = data.getData().get("content").toString();
                NlpResult nlpResult = hanlp.analyze(content);
                PageData theData = new PageData(data.getUrl(),
                        System.currentTimeMillis(),
                        gson.fromJson(gson.toJson(nlpResult), new TypeToken<HashMap<String, Object>>() {
                        }.getType()));

                putResult(taskId, theData);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Put data failed for " + data.getUrl() + " : " + e.toString());
            }
        }
    }

    private PageData[] getData(String url) throws IOException {
        String string = client.getAsString(url);
        Response response = gson.fromJson(string, Response.class);
        if (response.getStatus() != Response.Status.ok)
            throw new IOException("Master info: " + response.getData());

        response = gson.fromJson(string,
                new TypeToken<Response<PageData[]>>() {
                }.getType());
        return ((PageData[]) response.getData());
    }

    private void putResult(String id, PageData data) throws IOException {
        String string = client.putString(baseUrl + String.format(dataSubmitUrl, "nlp" + id), gson.toJson(data));
        logger.log(Level.FINE, string);
    }
}
