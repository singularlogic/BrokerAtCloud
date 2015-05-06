package org.broker.orbi.util.sc3;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.broker.orbi.util.entities.OrbiServiceDescription;

/**
 *
 * @author smantzouratos
 */
public class OrbiSDValidationPublisher {

    private static final String sdValidationMechanismURL = "http://213.249.38.66:3335/org.seerc.brokeratcloud.webservice/rest/topics/evaluation/SD/";

    public static boolean execute(OrbiServiceDescription _orbiSD) {

        boolean sdValidated = false;

        Logger.getLogger(OrbiSDValidationPublisher.class.getName()).log(Level.INFO, "OrbiSDValidationPublisher invoked()");
        try {

            final HttpClient client = new HttpClient();

            PostMethod method = new PostMethod(sdValidationMechanismURL);

            RequestEntity re = new StringRequestEntity(_orbiSD.getSD(), "text/plain", "UTF-8");

            method.setRequestEntity(re);

            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {

                Logger.getLogger(OrbiSDValidationPublisher.class.getName()).log(Level.SEVERE, "{0}Method failed: ", method.getStatusLine());

            } else {

                byte[] bytesArray = IOUtils.toByteArray(method.getResponseBodyAsStream());

                String response = new String(bytesArray);

                if (response.equalsIgnoreCase("OK")) {

                    sdValidated = true;

                    Logger.getLogger(OrbiSDValidationPublisher.class.getName()).info("Method response: Orbi SD validated successfully!");

                }

            }

            method.releaseConnection();

        } catch (IOException ex) {
            Logger.getLogger(OrbiSDValidationPublisher.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sdValidated;
    }

}
