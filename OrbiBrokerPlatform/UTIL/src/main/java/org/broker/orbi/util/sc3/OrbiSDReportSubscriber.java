package org.broker.orbi.util.sc3;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author smantzouratos
 */
public class OrbiSDReportSubscriber {

    private static final String sdReportMechanismURL = "http://213.249.38.66:3335/org.seerc.brokeratcloud.webservice/rest/subscriptions/evaluation/SDReport?wsCallbackEndpoint=";

    private static final String orbiCallbackURL = "http://orbidev.singularlogic.eu/sdreport/";

    public static boolean execute() {

        boolean sdReportSubscribed = false;

        Logger.getLogger(OrbiSDReportSubscriber.class.getName()).log(Level.INFO, "OrbiSDReportSubscriber invoked()");

        try {

            final HttpClient client = new HttpClient();

            PutMethod method = new PutMethod(sdReportMechanismURL + orbiCallbackURL);

            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {

                Logger.getLogger(OrbiSDReportSubscriber.class.getName()).log(Level.SEVERE, "Method failed: {0}", method.getStatusLine());

            } else {

                byte[] bytesArray = IOUtils.toByteArray(method.getResponseBodyAsStream());

                String response = new String(bytesArray);

                if (response.equalsIgnoreCase("OK")) {

                    sdReportSubscribed = true;

                    Logger.getLogger(OrbiSDReportSubscriber.class.getName()).info("Method response: Successfully subscribed to SD Report!");

                }

            }

            method.releaseConnection();
        } catch (IOException ex) {
            Logger.getLogger(OrbiSDReportSubscriber.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sdReportSubscribed;

    }
}