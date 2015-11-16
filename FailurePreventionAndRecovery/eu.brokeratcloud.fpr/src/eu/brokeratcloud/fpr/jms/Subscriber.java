package eu.brokeratcloud.fpr.jms;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import eu.brokeratcloud.fpr.PropertiesUtil;
import eu.brokeratcloud.fpr.resources.PubSub;

public class Subscriber implements MessageListener {

	public static String pubsubServer = null;

	public static void startListening() throws JMSException {
		// public static void main(String[] args) throws JMSException{

		System.setProperty("java.naming.factory.initial", "org.wso2.andes.jndi.PropertiesFileInitialContextFactory");
		String address = PropertiesUtil.INSTANCE.get("pubsubServer");
		pubsubServer = address;
		System.out.println(">>>>>>>" + address);
		System.setProperty("connectionfactory.ConnectionFactory", address);

		TopicConnectionFactory topicConnectionFactory = JNDIContext.getInstance().getTopicConnectionFactory();
		TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
		TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

		listenTo(session, "AvgQueryTime");
		listenTo(session, "ImpendingFailureHigh");
		listenTo(session, "ImpendingFailureLow");
		listenTo(session, "ImpendingFailureMedium");
		listenTo(session, "OccurredFailure");
		listenTo(session, "RecoveredFailure");

		topicConnection.start();
		System.out.println("started");
	}

	private String topic = "";

	public Subscriber(String topic) {
		this.topic = topic;
	}

	public static void listenTo(TopicSession session, String topicName) throws JMSException {
		Topic topic = session.createTopic(topicName);
		TopicSubscriber sub = session.createSubscriber(topic);
		sub.setMessageListener(new Subscriber(topicName));
	}

	@Override
	public void onMessage(Message arg0) {

		// System.out.println(arg0);
		try {
			TextMessage textMessage = (TextMessage) arg0;
			String s = textMessage.getText();
			String cep = String.format("{ \"%s\": %s }", topic, s);
			System.out.println(cep);
			new PubSub().cepEvent(cep);
		} catch (JMSException e) {

			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws JMSException {
		startListening();
	}

}
