package org.broker.orbi.rest.client;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class PuLSaRRestClient {

    public static final String PULSAR_REST_URL = "http://192.168.3.34:9999/user/";

    private final static Client client = Client.create();

    public static PuLSaRResponse.StatusCode isUsernameExists(String username) {
        WebResource webResource = client.resource(PULSAR_REST_URL.concat("get"));

        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        if (response.getStatus() != 200) {
            Logger.getLogger(BrokerRestClient.class.getName()).severe("Failed on URL: " + PULSAR_REST_URL + " : HTTP error code : " + response.getStatus());
            return PuLSaRResponse.StatusCode.SERVICE_DOWN;

        } else {
            List<PuLSaRUser> pulsarUsers = new Gson().fromJson(response.getEntity(String.class), new TypeToken<List<PuLSaRUser>>() {
            }.getType());

            for (PuLSaRUser user : pulsarUsers) {
                if (username.equals(user.getUsername())) {
                    return PuLSaRResponse.StatusCode.USER_EXISTS;
                }
            }
            return PuLSaRResponse.StatusCode.USER_NOT_EXIST;
        }
    }

    public static PuLSaRResponse.StatusCode addPuLSarUser(PuLSaRUser user) {
        WebResource webResource = client.resource(PULSAR_REST_URL.concat("add")).queryParam("username", user.getUsername()).queryParam("password", user.getPassword()).queryParam("roles", user.getRoles());
        System.out.println(user.toString());
        ClientResponse response = webResource.post(ClientResponse.class);

        if (response.getStatus() != 200) {
            Logger.getLogger(BrokerRestClient.class.getName()).severe("Failed on URL: " + PULSAR_REST_URL + " : HTTP error code : " + response.getStatus());
            return PuLSaRResponse.StatusCode.SERVICE_DOWN;
        } else {
            //TODO: Check if user is added!
        }
        return PuLSaRResponse.StatusCode.USER_ADDED;
    }

    public static void main(String ars[]) {
//        PuLSaRUser user = new PuLSaRUser("test3", "test3", "sc");
//
//        PuLSaRRestClient.addPuLSarUser(user);
        

        
    }
    
    

        
        
        
}
