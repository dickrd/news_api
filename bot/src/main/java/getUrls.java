import com.google.gson.Gson;
import content.PageData;
import content.Record;
import message.Assignment;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import util.GetUrlsFromRedis;
import webpages.NeteaseNews;
import webpages.WeiboPage;

import java.io.IOException;


/**
 * Created by first1hand on 2017/4/5.
 */
public class getUrls {
    public static void main(String[] args) throws IOException {

        GetUrlsFromRedis getUrlsFromRedis = new GetUrlsFromRedis();
        String keys[] = {"news.163.com", "weibo.com"};

        while(true) {
            for (String key : keys) {
                getUrlsFromRedis.getTaskResponse(key);
                Assignment taskAssignment = getUrlsFromRedis.getTaskAssignment();
                if(taskAssignment!=null){
                    String taskId = taskAssignment.getId();
                    for (String url : taskAssignment.getTasks()) {
                        Record record = new Record();
                        switch (key) {
                            case "weibo.com":
                                record = new WeiboPage(taskId, url).dealWeiboPage();
                                break;
                            case "news.163.com":
                                record = new NeteaseNews(taskId, url).dealNeteasenews();
                                break;
                            default:
                                break;
                        }
                        if(record!=null){
                            String taskInfo = Jsoup.connect("http://192.168.1.24:666/data/" + record.getTaskId())
                                    .requestBody(new Gson().toJson(new PageData(record.getUrl(), System.currentTimeMillis(), record)))
                                    .header("Content-Type", "application/json").method(Connection.Method.PUT).ignoreContentType(true).execute().body();
                            //System.out.println(taskInfo);
                        }
                    }
                }
            }
        }
    }
}
