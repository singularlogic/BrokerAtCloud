package org.broker.orbi.topic;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import org.broker.orbi.topic.message.BrokerTopicMessage;

/**
 *
 * @author smantzouratos
 */
public class BrokerTopicSubscriber {

    static TopicConnection conn = null;
    static TopicSession session = null;
    static Topic topic = null;

    public static String sendMsg(String jsonObject, String zabbixKey) {
        
        String response = null;
        
        InitialContext iniCtx = null;

        try {
            iniCtx = new InitialContext();

            Object tmp = iniCtx.lookup("jms/BrokerTopicConnectionFactory");
            TopicConnectionFactory tcf = (TopicConnectionFactory) tmp;
            conn = tcf.createTopicConnection("admin", "ub1t3ch!");
            topic = (Topic) iniCtx.lookup("java:jboss/queues/BrokerTopic");
            session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);

            TopicPublisher send = session.createPublisher(topic);

            BrokerTopicMessage brokerMsg = new BrokerTopicMessage();
            
            brokerMsg.setMsgDate(new Date());
            brokerMsg.setMsgSubject(zabbixKey);
            brokerMsg.setMsgTo("Fail Prevention System");
            brokerMsg.setMsgType("JSON");
            brokerMsg.setMsgBody(jsonObject);
            
            ObjectMessage objMsg = session.createObjectMessage(brokerMsg);

            TopicSubscriber recv = session.createSubscriber(topic);

            conn.start();

            send.publish(objMsg);

            send.close();

            session.close();

            conn.close();

        } catch (NamingException ex) {
            Logger.getLogger(BrokerTopicSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        } catch (JMSException ex) {
            Logger.getLogger(BrokerTopicSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(BrokerTopicSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }
        
        return response;
    }
}
