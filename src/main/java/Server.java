import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import resource.Search;
import resource.Task;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class Server extends Application {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) {
        URI baseUri = UriBuilder.fromUri("http://192.168.1.24/").port(666).build();
        ResourceConfig config = new ResourceConfig(Search.class, Task.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                server.start();
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Server down!", e);
            }
        }
    }
}
