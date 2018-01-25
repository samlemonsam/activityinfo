package chdc.server;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/app")
public class FrontendResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getHostPage() throws IOException {
        return Resources.toString(Resources.getResource("/host.html"), Charsets.UTF_8);
    }

}
