package eu.brokeratcloud.fpr.jms;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import eu.brokeratcloud.fpr.PropertiesUtil;

public class TestProducer {

	public static void main(String[] args) throws JMSException {
		sendMessage("monitoring/ImpendingFailureHigh", "{\"Service\":\"sp:Calendar\",\"Cause\":\"LongResponse\"}");

	}

	public static void sendMessage(String topic, String message) throws JMSException{
		System.setProperty("java.naming.factory.initial", "org.wso2.andes.jndi.PropertiesFileInitialContextFactory");
		String address = PropertiesUtil.INSTANCE.get("pubsubServer");
		String pubsubServer = address;
		System.out.println(">>>>>>>" + address);
		System.setProperty("connectionfactory.ConnectionFactory", address);

		TopicConnectionFactory topicConnectionFactory = JNDIContext.getInstance().getTopicConnectionFactory();
		TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
		TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		Subscriber.session = session;
		Topic jmstopic = session.createTopic(topic);
		TopicPublisher publisher = session.createPublisher(jmstopic);
		TextMessage textmessage = session.createTextMessage(message);
		publisher.send(textmessage);
		System.out.println("sent");
	}
}
