package carve.greeting;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/greeting")
public class GreetingResource {

    @GET
    @Path("/")
    @Produces("text/plain")
    public String world() {
        return "hello world";
    }
}
