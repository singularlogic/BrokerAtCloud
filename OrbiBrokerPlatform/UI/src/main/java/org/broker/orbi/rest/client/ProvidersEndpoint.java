package org.broker.orbi.rest.client;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.broker.orbi.models.ServiceDescription;
import org.broker.orbi.service.ServiceDescriptionService;
import org.json.JSONArray;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@Path("/v1")
public class ProvidersEndpoint {

    @GET
    @POST
    @Produces("application/json")
    @Path("/providers/{username}")
    public Response getProviders(@PathParam("username") String username) {
        List<ServiceDescription>  sds = ServiceDescriptionService.getServiceDescriptions(username);
        JSONArray jsonSD = new JSONArray();
        for (ServiceDescription sd : sds){
            jsonSD.put(sd.getFull_name());
        }
        return Response.status(200).entity(jsonSD.toString()).build();
    }
}
