package org.broker.orbi.util.other;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class VMUtilization {

    private final static Client client = Client.create();

    public static String getServerInfo(String serverURL) {
        Logger.getLogger(VMUtilization.class.getName()).info("VM URL to get info is: " + serverURL);

        WebResource webResource = client.resource(serverURL);
        String responseMsg = "";

        try {
            ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            if (response.getStatus() == 200) {
                responseMsg = response.getEntity(String.class);
//            Logger.getLogger(VMUtilization.class.getName()).info("HTTP status code : " + response.getStatus() + " Rsponse: " + responseMsg);
            }

        } catch (Exception ex) {

            Logger.getLogger(VMUtilization.class.getName()).warning("VM with URL:  " + serverURL + " is not initialized yet.. ");
        }

        return responseMsg;
    }

}
