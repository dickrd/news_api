package util;

import com.google.gson.Gson;
import message.TaskAssignment;
import message.TaskRequest;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import resource.Task;


import java.io.IOException;


/**
 * Created by first1hand on 2017/4/5.
 */
public class GetUrlsFromRedis {
    private TaskAssignment taskAssignment;

    public TaskAssignment getTaskAssignment() {
        return taskAssignment;
    }

    public void getTaskResponse(String key){
        try {
            String taskInfo = Jsoup.connect("http://192.168.1.24:666/task")
                    .requestBody("{    \""+key+"\":[\"\"],   \"urlSize\":2}")
                    .header("Content-Type", "application/json").method(Connection.Method.POST).ignoreContentType(true).execute().body();
            taskAssignment = new Gson().fromJson(taskInfo,TaskAssignment.class);
            System.out.println(taskInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
