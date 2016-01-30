package org.broker.orbi.rest.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import sun.misc.BASE64Decoder;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class BrokerRestClient {

    public static final String BROKER_POLICY_VALIDATION_URL = "http://broker.euprojects.net:8080/org.seerc.brokeratcloud.webservice/rest/brokerPolicy/upload";
    public static final String BROKER_SERVICEDESC_VALIDATION_URL = "http://broker.euprojects.net:8080/org.seerc.brokeratcloud.webservice/rest/serviceDescription/validate";
    public static final String BROKER_SERVICEDESC_VALIDATION_URL_SECONDARY = "http://broker.euprojects.net:8080/org.seerc.brokeratcloud.webservice/rest/topics/evaluation/SD/SiLo";

    private final static Client client = Client.create();

    private final static String SAMPLE_BROKER_POLICY = "QHByZWZpeCBmb2FmOiA8aHR0cDovL3htbG5zLmNvbS9mb2FmLzAuMS8+IC4gCkBwcmVmaXggcmRmOiA8aHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIz4gLiAKQHByZWZpeCByZGZzOiA8aHR0cDovL3d3dy53My5vcmcvMjAwMC8wMS9yZGYtc2NoZW1hIz4gLiAKQHByZWZpeCBvd2w6IDxodHRwOi8vd3d3LnczLm9yZy8yMDAyLzA3L293bCM+IC4gCkBwcmVmaXggZGN0ZXJtczogPGh0dHA6Ly9wdXJsLm9yZy9kYy90ZXJtcy8+IC4gCkBwcmVmaXggdXNkbC1jb3JlOiA8aHR0cDovL3d3dy5saW5rZWQtdXNkbC5vcmcvbnMvdXNkbC1jb3JlIz4gLiAKQHByZWZpeCB1c2RsLWNvcmUtY2I6IDxodHRwOi8vd3d3LmxpbmtlZC11c2RsLm9yZy9ucy91c2RsLWNvcmUvY2xvdWQtYnJva2VyIz4gLiAKQHByZWZpeCB1c2RsLXNsYTogPGh0dHA6Ly93d3cubGlua2VkLXVzZGwub3JnL25zL3VzZGwtc2xhIz4gLiAKQHByZWZpeCB1c2RsLXNsYS1jYjogPGh0dHA6Ly93d3cubGlua2VkLXVzZGwub3JnL25zL3VzZGwtY29yZS9jbG91ZC1icm9rZXItc2xhIz4gLiAKQHByZWZpeCB1c2RsLXByZWY6IDxodHRwOi8vd3d3LmxpbmtlZC11c2RsLm9yZy9ucy91c2RsLXByZWYjPiAuIApAcHJlZml4IHVzZGwtYnVzaW5lc3Mtcm9sZXM6IDxodHRwOi8vd3d3LmxpbmtlZC11c2RsLm9yZy9ucy91c2RsLWJ1c2luZXNzLXJvbGVzIz4gLiAKQHByZWZpeCBibHVlcHJpbnQ6IDxodHRwOi8vYml6d2ViLnNhcC5jb20vVFIvYmx1ZXByaW50Iz4gLiAKQHByZWZpeCB2Y2FyZDogPGh0dHA6Ly93d3cudzMub3JnLzIwMDYvdmNhcmQvbnMjPiAuIApAcHJlZml4IHhzZDogPGh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIz4gLiAKQHByZWZpeCBjdGFnOiA8aHR0cDovL2NvbW1vbnRhZy5vcmcvbnMjPiAuIApAcHJlZml4IG9yZzogPGh0dHA6Ly93d3cudzMub3JnL25zL29yZyM+IC4gCkBwcmVmaXggc2tvczogPGh0dHA6Ly93d3cudzMub3JnLzIwMDQvMDIvc2tvcy9jb3JlIz4gLiAKQHByZWZpeCB0aW1lOiA8aHR0cDovL3d3dy53My5vcmcvMjAwNi90aW1lIz4gLiAKQHByZWZpeCBncjogPGh0dHA6Ly9wdXJsLm9yZy9nb29kcmVsYXRpb25zL3YxIz4gLiAKQHByZWZpeCBkb2FwOiA8aHR0cDovL3VzZWZ1bGluYy5jb20vbnMvZG9hcCM+IC4gCkBwcmVmaXggZmM6IDxodHRwOi8vd3d3LmJyb2tlci1jbG91ZC5ldS9zZXJ2aWNlLWRlc2NyaXB0aW9ucy9DQVMvY2F0ZWdvcmllcyM+IC4gCkBwcmVmaXggYnA6IDxodHRwOi8veW91cmJyb2tlcm5hbWVzcGFjZS5jb20jPiAuIAoKYnA6WW91ckJyb2tlckNvbXBhbnlBY3JvbnltIAogIGEgZ3I6QnVzaW5lc3NFbnRpdHkgOyAKICBncjpsZWdhbE5hbWUgIllvdXIgQnJva2VyQ29tcGFueSBMZWdhbCBOYW1lIl5eeHNkOnN0cmluZyAuIAoKYnA6WW91ckJyb2tlckNvbXBhbnlBY3JvbnltRW50aXR5SW52b2x2ZW1lbnQgCiAgYSB1c2RsLWNvcmU6RW50aXR5SW52b2x2ZW1lbnQgOyAKICB1c2RsLWNvcmU6d2l0aEJ1c2luZXNzUm9sZSB1c2RsLWJ1c2luZXNzLXJvbGVzOmludGVybWVkaWFyeSA7IAogIHVzZGwtY29yZTpvZkJ1c2luZXNzRW50aXR5IGJwOllvdXJCcm9rZXJDb21wYW55QWNyb255bSAuIAoKYnA6WW91ckJyb2tlclBvbGljeVRpdGxlIAogIGEgYnA6WW91ckJyb2tlclBvbGljeU1vZGVsTmFtZSA7IAogIGRjdGVybXM6Y3JlYXRvciBicDpZb3VyQnJva2VyQ29tcGFueUFjcm9ueW0gOyAKICB1c2RsLWNvcmU6aGFzRW50aXR5SW52b2x2ZW1lbnQgYnA6WW91ckJyb2tlckNvbXBhbnlBY3JvbnltRW50aXR5SW52b2x2ZW1lbnQgOyAKICB1c2RsLWNvcmUtY2I6aGFzQ2xhc3NpZmljYXRpb25EaW1lbnNpb24gZmM6cm9vdENvbmNlcHQgLiAKCmJwOllvdXJCcm9rZXJQb2xpY3lNb2RlbE5hbWUgCiAgcmRmczpzdWJDbGFzc09mIHVzZGwtY29yZTpTZXJ2aWNlTW9kZWwgLiAKCmJwOlNvbWVTZXJ2aWNlTGV2ZWxQcm9maWxlIAogIHJkZnM6c3ViQ2xhc3NPZiB1c2RsLXNsYTpTZXJ2aWNlTGV2ZWxQcm9maWxlIC4gCgpicDpoYXNTb21lU2VydmljZUxldmVsUHJvZmlsZSAKICByZGZzOnN1YlByb3BlcnR5T2YgdXNkbC1zbGE6aGFzU2VydmljZUxldmVsUHJvZmlsZSA7IAogIHJkZnM6ZG9tYWluIGJwOllvdXJCcm9rZXJQb2xpY3lNb2RlbE5hbWUgOyAKICByZGZzOnJhbmdlIGJwOlNvbWVTZXJ2aWNlTGV2ZWxQcm9maWxlIC4gCgpmYzpZb3VyQnJva2VyUG9saWN5VGl0bGVDb25jZXB0U2NoZW1lIAogIGEgc2tvczpDb25jZXB0U2NoZW1lIDsgCiAgZGN0ZXJtczp0aXRsZSAiUm9vdCBmb3IgYWxsIGNsYXNzaWZpY2F0aW9uIGRpbWVuc2lvbnMuIiA7IAogIHNrb3M6cHJlZkxhYmVsICJSb290IENvbmNlcHQgU2NoZW1lIkBlbiA7IAogIHNrb3M6aGFzVG9wQ29uY2VwdCBmYzpyb290Q29uY2VwdCAuIAoKZmM6cm9vdENvbmNlcHQgCiAgYSB1c2RsLWNvcmUtY2I6Q2xhc3NpZmljYXRpb25EaW1lbnNpb24gOyAKICBza29zOnByZWZMYWJlbCAiUm9vdCBDb25jZXB0IkBlbiA7IAogIHNrb3M6dG9wQ29uY2VwdE9mIGZjOllvdXJCcm9rZXJQb2xpY3lUaXRsZUNvbmNlcHRTY2hlbWUgOyAKICBza29zOmluU2NoZW1lIGZjOllvdXJCcm9rZXJQb2xpY3lUaXRsZUNvbmNlcHRTY2hlbWUgLiAKCmJwOlNMYWEgCiAgcmRmczpzdWJDbGFzc09mIHVzZGwtc2xhOlNlcnZpY2VMZXZlbCAuIAoKYnA6aGFzU0xhYSAKICByZGZzOnN1YlByb3BlcnR5T2YgdXNkbC1zbGE6aGFzU2VydmljZUxldmVsIDsgCiAgcmRmczpkb21haW4gYnA6U29tZVNlcnZpY2VMZXZlbFByb2ZpbGUgOyAKICByZGZzOnJhbmdlIGJwOlNMYWEgLiAKCmJwOlNMRWFhIAogIHJkZnM6c3ViQ2xhc3NPZiB1c2RsLXNsYTpTZXJ2aWNlTGV2ZWxFeHByZXNzaW9uIC4gCgpicDpoYXNTTEVhYSAKICByZGZzOnN1YlByb3BlcnR5T2YgdXNkbC1zbGE6aGFzU2VydmljZUxldmVsRXhwcmVzc2lvbiA7IAogIHJkZnM6ZG9tYWluIGJwOlNMYWEgOyAKICByZGZzOnJhbmdlIGJwOlNMRWFhIC4gCgpicDpWYXJhYSAKICByZGZzOnN1YkNsYXNzT2YgdXNkbC1zbGE6VmFyaWFibGUgLiAKCmJwOmhhc1ZhcmFhIAogIHJkZnM6c3ViUHJvcGVydHlPZiB1c2RsLXNsYTpoYXNWYXJpYWJsZSA7IAogIHJkZnM6ZG9tYWluIGJwOlNMRWFhIDsgCiAgcmRmczpyYW5nZSBicDpWYXJhYSAuIAoKYnA6YWEgCiAgcmRmczpzdWJDbGFzc09mIGdyOlF1YWxpdGF0aXZlVmFsdWUgOyAKICByZGZzOmxhYmVsICJhYSJeXnhzZDpzdHJpbmcgLCAKICAgICJhYSJAZW4gOyAKICBhIGJwOmFhIC4gCgpicDpoYXNEZWZhdWx0YWEgCiAgcmRmczpzdWJQcm9wZXJ0eU9mIHVzZGwtc2xhLWNiOmhhc0RlZmF1bHRRdWFsaXRhdGl2ZVZhbHVlIDsgCiAgcmRmczpkb21haW4gYnA6VmFyYWEgOyAKICByZGZzOnJhbmdlIGJwOmFhIC4gCgpicDpoYXNhYSAKICByZGZzOnN1YlByb3BlcnR5T2YgZ3I6cXVhbGl0YXRpdmVQcm9kdWN0T3JTZXJ2aWNlUHJvcGVydHkgOyAKICByZGZzOmRvbWFpbiBicDpZb3VyQnJva2VyUG9saWN5TW9kZWxOYW1lIDsgCiAgcmRmczpyYW5nZSBicDphYSAuIAoKYnA6YWFQcmVmZXJlbmNlVmFyaWFibGUgCiAgcmRmczpzdWJDbGFzc09mIHVzZGwtcHJlZjpRdWFsaXRhdGl2ZVZhcmlhYmxlIDsgCiAgdXNkbC1wcmVmOmJlbG9uZ3NUbyBmYzpyb290Q29uY2VwdCA7IAogIHVzZGwtcHJlZjpyZWZUb1NlcnZpY2VBdHRyaWJ1dGUgPGh0dHA6Ly93d3cuYnJva2VyYXRjbG91ZC5ldS92MS9vcHQvU0VSVklDRS1BVFRSSUJVVEUjbnVsbD4gLiAKCmJwOmhhc0RlZmF1bHRQcmVmVmFyYWEgCiAgcmRmczpzdWJQcm9wZXJ0eU9mIHVzZGwtcHJlZjpoYXNEZWZhdWx0UXVhbGl0YXRpdmVWYWx1ZSA7IAogIHJkZnM6ZG9tYWluIGJwOmFhUHJlZmVyZW5jZVZhcmlhYmxlIDsgCiAgcmRmczpyYW5nZSBicDphYSAuIAoKYnA6U0xzcyAKICByZGZzOnN1YkNsYXNzT2YgdXNkbC1zbGE6U2VydmljZUxldmVsIC4gCgpicDpoYXNTTHNzIAogIHJkZnM6c3ViUHJvcGVydHlPZiB1c2RsLXNsYTpoYXNTZXJ2aWNlTGV2ZWwgOyAKICByZGZzOmRvbWFpbiBicDpTb21lU2VydmljZUxldmVsUHJvZmlsZSA7IAogIHJkZnM6cmFuZ2UgYnA6U0xzcyAuIAoKYnA6U0xFc3MgCiAgcmRmczpzdWJDbGFzc09mIHVzZGwtc2xhOlNlcnZpY2VMZXZlbEV4cHJlc3Npb24gLiAKCmJwOmhhc1NMRXNzIAogIHJkZnM6c3ViUHJvcGVydHlPZiB1c2RsLXNsYTpoYXNTZXJ2aWNlTGV2ZWxFeHByZXNzaW9uIDsgCiAgcmRmczpkb21haW4gYnA6U0xzcyA7IAogIHJkZnM6cmFuZ2UgYnA6U0xFc3MgLiAKCmJwOlZhcnNzIAogIHJkZnM6c3ViQ2xhc3NPZiB1c2RsLXNsYTpWYXJpYWJsZSAuIAoKYnA6aGFzVmFyc3MgCiAgcmRmczpzdWJQcm9wZXJ0eU9mIHVzZGwtc2xhOmhhc1ZhcmlhYmxlIDsgCiAgcmRmczpkb21haW4gYnA6U0xFc3MgOyAKICByZGZzOnJhbmdlIGJwOlZhcnNzIC4gCgpicDpzcyAKICByZGZzOnN1YkNsYXNzT2YgZ3I6UXVhbGl0YXRpdmVWYWx1ZSA7IAogIHJkZnM6bGFiZWwgInNzcyJeXnhzZDpzdHJpbmcgLiAKCmJwOmhhc0RlZmF1bHRzcyAKICByZGZzOnN1YlByb3BlcnR5T2YgdXNkbC1zbGEtY2I6aGFzRGVmYXVsdFF1YWxpdGF0aXZlVmFsdWUgOyAKICByZGZzOmRvbWFpbiBicDpWYXJzcyA7IAogIHJkZnM6cmFuZ2UgYnA6c3MgLiAKCmJwOmhhc3NzIAogIHJkZnM6c3ViUHJvcGVydHlPZiBncjpxdWFsaXRhdGl2ZVByb2R1Y3RPclNlcnZpY2VQcm9wZXJ0eSA7IAogIHJkZnM6ZG9tYWluIGJwOllvdXJCcm9rZXJQb2xpY3lNb2RlbE5hbWUgOyAKICByZGZzOnJhbmdlIGJwOnNzIC4gCgpicDpzc1ByZWZlcmVuY2VWYXJpYWJsZSAKICByZGZzOnN1YkNsYXNzT2YgdXNkbC1wcmVmOlF1YWxpdGF0aXZlVmFyaWFibGUgOyAKICB1c2RsLXByZWY6YmVsb25nc1RvIGZjOnJvb3RDb25jZXB0IDsgCiAgdXNkbC1wcmVmOnJlZlRvU2VydmljZUF0dHJpYnV0ZSA8aHR0cDovL3d3dy5icm9rZXJhdGNsb3VkLmV1L3YxL29wdC9TRVJWSUNFLUFUVFJJQlVURSNudWxsPiAuIAoKYnA6aGFzRGVmYXVsdFByZWZWYXJzcyAKICByZGZzOnN1YlByb3BlcnR5T2YgdXNkbC1wcmVmOmhhc0RlZmF1bHRRdWFsaXRhdGl2ZVZhbHVlIDsgCiAgcmRmczpkb21haW4gYnA6c3NQcmVmZXJlbmNlVmFyaWFibGUgOyAKICByZGZzOnJhbmdlIGJwOnNzIC4gCg==";

    public static String validateBrokerPolicy(String policyContent) {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] decodedBytes = "".getBytes();
        String policyContentDecoded = "";
        try {
            decodedBytes = decoder.decodeBuffer(policyContent);
            policyContentDecoded = new String(decodedBytes, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(BrokerRestClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        String responseMessage = "";
        WebResource webResource = client.resource(BROKER_POLICY_VALIDATION_URL);
        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).put(ClientResponse.class, policyContentDecoded);
        if (response.getStatus() != 200) {
            Logger.getLogger(BrokerRestClient.class.getName()).severe("Failed on URL: " + BROKER_POLICY_VALIDATION_URL + " : HTTP error code : " + response.getStatus());
            responseMessage = "An unexpected error occured, please try again.";
        } else {
            responseMessage = response.getEntity(String.class);
        }
        return responseMessage;
    }

    public static String validateBrokerServiceDescription(String serviceDescriptionContent) {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] decodedBytes = "".getBytes();
        String policyContentDecoded = "";
        try {
            decodedBytes = decoder.decodeBuffer(serviceDescriptionContent);
            policyContentDecoded = new String(decodedBytes, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(BrokerRestClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        String responseMessage = "";
        WebResource webResource = client.resource(BROKER_SERVICEDESC_VALIDATION_URL);
        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, policyContentDecoded);
        if (response.getStatus() != 200) {
            Logger.getLogger(BrokerRestClient.class.getName()).severe("Failed on URL: " + BROKER_SERVICEDESC_VALIDATION_URL + " : HTTP error code : " + response.getStatus());
            responseMessage = "An unexpected error occured, please try again.";
        } else {
            responseMessage = response.getEntity(String.class);
            //Actual validation
            webResource = client.resource(BROKER_SERVICEDESC_VALIDATION_URL_SECONDARY);
            response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, policyContentDecoded);
            Logger.getLogger(BrokerRestClient.class.getName()).info("Service Description Validation response is: " + response.getStatus());
        }
        return responseMessage;
    }

    public static void main(String ars[]) {
        System.out.println(BrokerRestClient.validateBrokerPolicy(SAMPLE_BROKER_POLICY));

    }
}
