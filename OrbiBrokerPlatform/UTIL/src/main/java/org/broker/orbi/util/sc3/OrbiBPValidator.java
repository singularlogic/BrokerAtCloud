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
import org.broker.orbi.util.entities.OrbiBrokerPolicy;

/**
 *
 * @author smantzouratos
 */
public class OrbiBPValidator {

    private static final String bpValidationMechanismURL = "http://213.249.38.66:3335/org.seerc.brokeratcloud.webservice/rest/brokerPolicy/validate";

    public static boolean execute(OrbiBrokerPolicy _orbiBP) {

        boolean bpValidated = false;

        Logger.getLogger(OrbiBPValidator.class.getName()).log(Level.INFO, "OrbiBPValidator invoked()");
        try {
            final HttpClient client = new HttpClient();

            PostMethod method = new PostMethod(bpValidationMechanismURL);

            RequestEntity re = new StringRequestEntity(_orbiBP.getBP(), "text/plain", "UTF-8");

            method.setRequestEntity(re);

            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {

                Logger.getLogger(OrbiBPValidator.class.getName()).log(Level.SEVERE, "Method failed: {0}", method.getStatusLine());

            } else {

                byte[] bytesArray = IOUtils.toByteArray(method.getResponseBodyAsStream());

                String response = new String(bytesArray);

                if (response.equalsIgnoreCase("OK")) {

                    bpValidated = true;

                    Logger.getLogger(OrbiBPValidator.class.getName()).info("Method response: Orbi BP validated successfully!");

                }

            }
            method.releaseConnection();

        } catch (IOException ex) {
            Logger.getLogger(OrbiBPValidator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return bpValidated;

    }

}
