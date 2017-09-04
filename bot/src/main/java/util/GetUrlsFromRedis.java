package main.java.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.message.Assignment;
import main.java.message.Response;
import org.jsoup.Connection;
import org.jsoup.Jsoup;


/**
 * Created by first1hand on 2017/4/5.
 */
public class GetUrlsFromRedis {
    private Assignment taskAssignment;

    public Assignment getTaskAssignment() {
        return taskAssignment;
    }

    public void getTaskResponse(String key) {
        try {
            String taskInfo = Jsoup.connect("http://192.168.1.24:666/url/" + key + "?size=2")
                    .header("Content-Type", "application/json").method(Connection.Method.GET).ignoreContentType(true).execute().body();
            Response response = new Gson().fromJson(taskInfo, new TypeToken<Response>() {
            }.getType());
            if (response.getStatus() == Response.Status.ok) {
                response = new Gson().fromJson(taskInfo, new TypeToken<Response<Assignment>>() {
                }.getType());
                taskAssignment = (Assignment) response.getData();
            }else {
                taskAssignment = null;
                if (response.getStatus() == Response.Status.wait)
                    Thread.sleep(1000);
            }
            System.out.println(key+":"+taskInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
