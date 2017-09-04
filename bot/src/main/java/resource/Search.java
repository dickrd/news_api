package main.java.resource;

import com.google.gson.Gson;
import main.java.content.Record;
import main.java.message.QueryResponse;
import main.java.message.SearchRequest;
import main.java.message.SearchResponse;
import main.java.schedule.SingleThreadWorker;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */

@Path("/search")
public class Search {

    private Gson gson = new Gson();

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String search(String jsonString) {
        String id;
        try {
            SearchRequest searchRequest = gson.fromJson(jsonString, SearchRequest.class);
            id = SingleThreadWorker.feeds(searchRequest.getKeywords());
        } catch (Exception e) {
            id = "";
        }

        return gson.toJson(new SearchResponse(id));
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id) {
        Record[] records = SingleThreadWorker.query(id);

        return gson.toJson(new QueryResponse(id, records));
    }
}
