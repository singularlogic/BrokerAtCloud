package org.broker.orbi.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Path("/sdReport")
public class OrbiSDReportEndpoint {

    @POST
    public Response sdReportRetriever(String sdReport, @Context HttpHeaders headers) {
        Logger.getLogger(OrbiSDReportEndpoint.class.getName()).log(Level.INFO, "OrbiSDReportEndpoint invoked()");

        if (sdReport.equalsIgnoreCase("OK")) {

            Logger.getLogger(OrbiSDReportEndpoint.class.getName()).log(Level.INFO, "SD validated successfully!");

        } else {

            Logger.getLogger(OrbiSDReportEndpoint.class.getName()).log(Level.SEVERE, "SD validation failed!");

        }

        return Response.status(200).build();

    }

}
