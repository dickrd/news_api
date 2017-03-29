package resource;

import com.google.gson.Gson;
import message.SearchRequest;
import message.SearchResponse;
import schedule.SingleThreadWorker;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */

@Path("search")
public class Search {

    private Gson gson = new Gson();

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String search(String jsonString) {
        SearchRequest searchRequest = gson.fromJson(jsonString, SearchRequest.class);

        SingleThreadWorker singleThreadWorker = new SingleThreadWorker();
        String id = singleThreadWorker.feeds(searchRequest.getKeywords());

        return gson.toJson(new SearchResponse(id));
    }
}
