package main.java.resource;

import com.google.gson.Gson;
import main.java.message.TaskAssignment;
import main.java.message.TaskRequest;
import main.java.schedule.SingleThreadWorker;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Dick Zhou on 3/30/2017.
 *
 */
@Path("/task")
public class Task {
    private Gson gson = new Gson();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String get(String jsonString) {
        TaskAssignment taskAssignment;
        try {
            TaskRequest taskRequest = gson.fromJson(jsonString, TaskRequest.class);
            taskAssignment = SingleThreadWorker.dispatch(taskRequest.getNames(), taskRequest.getUrlSize());
        } catch (Exception e) {
            taskAssignment = new TaskAssignment("", new String[0]);
        }

        return gson.toJson(taskAssignment);
    }
}
