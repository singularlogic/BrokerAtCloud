package org.broker.orbi.topic.listener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.broker.orbi.topic.message.BrokerTopicMessage;

/**
 *
 * @author smantzouratos
 */
@TransactionManagement(value = TransactionManagementType.BEAN)
@MessageDriven(mappedName = "BrokerTopic", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode",
            propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "useJNDI", propertyValue = "true"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/queues/BrokerTopic")
})
public class BrokerTopicListener implements MessageListener {

    private static final String failPreventionMechanismURL = "http://94.75.243.141:8081/org.seerc.brokeratcloud.webservice/rest/topics/monitoring/monitoring/Sintef";
    private static final String failPreventionCustomURL = "http://94.75.243.141:8081/org.seerc.brokeratcloud.webservice/rest/topics/monitoring/";

    public BrokerTopicListener() {
    }

    @Override
    public void onMessage(Message message) {
        try {
//            init();

            BrokerTopicMessage brokerMsg = (BrokerTopicMessage) ((ObjectMessage) message).getObject();

            // After retrieving message from Monitoring Prob System the JSON will be posted to the fail prevention module
            String jsonMSG = brokerMsg.getMsgBody();

            String zabbixKey = brokerMsg.getMsgSubject();

            switch (zabbixKey) {
                case "system.memoryLoad":
                    postToFailPreventionMechanism(jsonMSG, zabbixKey, false);
                    break;
                case "system.storageLoad":
                    postToFailPreventionMechanism(jsonMSG, zabbixKey, false);
                    break;
                case "apache[ReqPerSec]":
                    postToFailPreventionMechanism(jsonMSG, zabbixKey, false);
                    break;
                case "mysql.threads":
                    postToFailPreventionMechanism(jsonMSG, zabbixKey, false);
                    break;
                case "system.cpu.util[,user]":
                    postToFailPreventionMechanism(jsonMSG, zabbixKey, false);
                    break;
                case "http":
                    postToFailPreventionMechanism(jsonMSG, zabbixKey, false);
                    break;
                default:
                    postToFailPreventionMechanism(jsonMSG, zabbixKey, true);
                    break;
            }

//            Logger.getLogger(BrokerTopicListener.class.getName()).log(Level.INFO, "Message@BrokerTopic: " + jsonMSG);
//            destroy();
        } catch (JMSException e) {
//            Logger.getLogger(BrokerTopicListener.class.getName()).severe(e.getMessage());
        } catch (Throwable te) {
//            Logger.getLogger(BrokerTopicListener.class.getName()).severe(te.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        System.out.println("Broker Topic Listener received a message..");

    }

    @PreDestroy
    public void destroy() {
        System.out.println("Broker Topic Listener proccesed a message!");
    }

    private boolean postToFailPreventionMechanism(String message, String zabbixKey, boolean mainURL) {

        try {
            // Create a method instance.

            final HttpClient client = new HttpClient();

            PostMethod method = null;

            if (mainURL) {

                method = new PostMethod(failPreventionMechanismURL);

            } else {

                String URL = "";
                switch (zabbixKey) {
                    case "system.memoryLoad":
                        URL = failPreventionCustomURL + "MemoryLoad/SiLo/";
                        break;
                    case "system.storageLoad":
                        URL = failPreventionCustomURL + "StorageLoad/SiLo/";
                        break;
                    case "apache[ReqPerSec]":
                        URL = failPreventionCustomURL + "RequestsPerSec/SiLo/";
                        break;
                    case "mysql.threads":
                        URL = failPreventionCustomURL + "Threads/SiLo/";
                        break;
                    case "system.cpu.util[,user]":
                        URL = failPreventionCustomURL + "CPULoadAvgPerCore/SiLo/";
                        break;
                    case "http":
                        URL = failPreventionCustomURL + "HTTPResponseTime/SiLo/";
                        break;
                    default:
                        URL = failPreventionMechanismURL;
                        break;
                }

                method = new PostMethod(URL);
            }

            RequestEntity re = new StringRequestEntity(message, "application/json", "UTF-8");

            method.setRequestEntity(re);

            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                Logger.getLogger(BrokerTopicListener.class.getName()).severe("Method failed: " + method.getStatusLine());
                return false;
            } else {
                byte[] bytesArray = IOUtils.toByteArray(method.getResponseBodyAsStream());
                Logger.getLogger(BrokerTopicListener.class.getName()).info("Method response: " + new String(bytesArray));
            }

            method.releaseConnection();

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BrokerTopicListener.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(BrokerTopicListener.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;

    }

}
