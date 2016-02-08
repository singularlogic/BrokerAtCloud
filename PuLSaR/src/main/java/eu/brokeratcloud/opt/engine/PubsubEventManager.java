/*
 * #%L
 * Preference-based cLoud Service Recommender (PuLSaR) - Broker@Cloud optimisation engine
 * %%
 * Copyright (C) 2014 - 2016 Information Management Unit, Institute of Communication and Computer Systems, National Technical University of Athens
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.brokeratcloud.opt.engine;

import eu.brokeratcloud.common.SLMEvent;
import eu.brokeratcloud.opt.RecommendationManager;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import javax.ws.rs.client.Entity;

class PubsubEventManager extends EventManager {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("eu.brokeratcloud.event.manager.pubsub");
	
	protected static final String defaultPubsubFile = "pubsub.properties";
	
	protected Configuration config;
	protected boolean online;
	protected RecommendationManager recomMgr;
	protected CallbackEndpointServer server;
	protected HashMap<String,String[][]> topics;
	protected String callbackHost;
	protected int callbackPort;
	
	public PubsubEventManager() throws IOException {
		this(defaultPubsubFile);
	}
	public PubsubEventManager(String pubsubFile) throws IOException {
		initPubsub(pubsubFile);
		recomMgr = RecommendationManager.getInstance();
		recomMgr.setEventManager(this);
		server = new CallbackEndpointServer(this, topics, callbackHost, callbackPort);
	}
	
	protected void initPubsub(String pubsubFile) throws IOException {
		topics = new HashMap<String,String[][]>();
		
		// Load pubsub properties
		InputStream is = getClass().getClassLoader().getResourceAsStream(pubsubFile);
		if (is==null) {
			logger.error("PubsubEventManager: Pubsub properties file not found: {}", pubsubFile);
			return;
		}
		Properties pubsubProps = new Properties();
		pubsubProps.load( is );
		
		// Process callback endpoint host and port
		String host = pubsubProps.getProperty("CALLBACK-HOST", ".AUTO");
		int port = Integer.parseInt( pubsubProps.getProperty("CALLBACK-PORT", "8080") );
		if (host==null || (host=host.trim()).isEmpty()) host = ".AUTO";
		String[] hostPart = host.split("[ \t]+");
		for (int i=0; i<hostPart.length; i++) hostPart[i] = hostPart[i].trim();
		if (hostPart[0].equalsIgnoreCase(".AUTO")) {
			logger.debug("PubsubEventManager: Resolving callback endpoint host dynamically");
			List<InetAddress> addresses = null;
			if (hostPart[1].equalsIgnoreCase("FILTER") && hostPart.length>2) {
				String filter = hostPart[2].replace(".","\\.").replace("*",".*");
				logger.debug("PubsubEventManager: Using address filter: {}", filter);
				addresses = NetworkHelper.getInetAddresses(filter);
			} else {
				addresses = NetworkHelper.getInetAddresses();
			}
			if (addresses.size()==0) {
				host = InetAddress.getLocalHost().getHostAddress();
				logger.warn("PubsubEventManager: No network addresses found. Using loopback: {}", host);
			} else {
				StringBuilder sb = new StringBuilder();
				for (InetAddress a : addresses) sb.append(a.getHostAddress()).append(" ");
				logger.info("PubsubEventManager: Found {} address(es): {}", +addresses.size(), sb.toString().trim());
				host = addresses.get(0).getHostAddress();
				if (addresses.size()>1) logger.info("PubsubEventManager: Using the first address: "+host);
				else logger.info("PubsubEventManager: Using address: "+host);
			}
		}
		
		Properties context = new Properties();
		context.setProperty("CALLBACK-HOST", host);
		context.setProperty("CALLBACK-PORT", Integer.toString(port));
		String base = pubsubProps.getProperty("CALLBACK-BASE");
		if (base!=null) {
			base = processPlaceholders(base, context);
			context.setProperty("CALLBACK-BASE", base);
		}
		
		this.callbackHost = host;
		this.callbackPort = port;
		logger.info("PubsubEventManager: Callback endpoint host={}, port={}, base={}", host, port, base);
		
		// Process Topic URLs (subscribe, unsubscribe, callback and publish URLs)
		for (Object o : pubsubProps.keySet()) {
			String p = o.toString();
			p = p.trim().toUpperCase();
			if ( ! p.startsWith("TOPIC-")) continue;
			int i1 = "TOPIC-".length();
			int i2 = p.lastIndexOf("-");
			if (i1>0 && i2>i1) {
				String topicName = p.substring(i1, i2).trim();
				if (topics.containsKey(topicName.toUpperCase())) continue;
				
				if (!topicName.isEmpty()) {
					String subscribeVal = pubsubProps.getProperty("TOPIC-"+topicName+"-SUBSCRIBE");
					String unsubscribeVal = pubsubProps.getProperty("TOPIC-"+topicName+"-UNSUBSCRIBE");
					String callbackVal = pubsubProps.getProperty("TOPIC-"+topicName+"-CALLBACK");
					String publishVal = pubsubProps.getProperty("TOPIC-"+topicName+"-PUBLISH");
					
					if (callbackVal==null || (callbackVal=callbackVal.trim()).isEmpty()) callbackVal = null;
					if (subscribeVal==null || (subscribeVal=subscribeVal.trim()).isEmpty()) subscribeVal = null;
					if (unsubscribeVal==null || (unsubscribeVal=unsubscribeVal.trim()).isEmpty()) unsubscribeVal = null;
					if (publishVal==null || (publishVal=publishVal.trim()).isEmpty()) publishVal = null;
					
					if (subscribeVal!=null && callbackVal==null || subscribeVal==null && publishVal==null) {
						logger.error("PubsubEventManager: Invalid specification for Topic: {} : At least subscribe & callback URLs or publich URL must be provided", topicName);
						continue;
					}
					if (subscribeVal==null && callbackVal!=null) {
						logger.warn("PubsubEventManager: Callback URL specified without subscribe URL for Topic: {} : Ignoring it", topicName);
					}
					if (subscribeVal==null && unsubscribeVal!=null) {
						logger.warn("PubsubEventManager: Unsubscribe URL specified without subscribe URL for Topic: {} : Ignoring it", topicName);
					}
					
					String[][] topicUrl = new String[4][];
					topicUrl[0] = prepareTopicUrl(callbackVal, "GET", context, "CALLBACK");
					topicUrl[1] = prepareTopicUrl(subscribeVal, "GET", context, "SUBSCRIBE");
					topicUrl[2] = prepareTopicUrl(unsubscribeVal, "GET", context, "UNSUBSCRIBE");
					topicUrl[3] = prepareTopicUrl(publishVal, "GET", context, "PUBLISH");
					
					topics.put(topicName.toUpperCase(), topicUrl);
				}
			}
		}
		
		if (topics.size()==0) {
			logger.error("PubsubEventManager: No topics found in Pubsub properties file: {}", pubsubFile);
		}
	}
	
	protected String[] prepareTopicUrl(String val, String defaultVerb, Properties context, String scope) {
		if (val==null || (val=val.trim()).isEmpty()) return null;
		String[] arr = val.split("[ \t]+",2);
		for (int i=0; i<arr.length; i++) arr[i] = arr[i].trim();
		
		if (arr.length==2 && arr[0].isEmpty()) {
			arr[0] = defaultVerb;
		} else
		if (arr.length==2 && arr[1].isEmpty()) {
			String url = arr[0];
			arr[0] = defaultVerb;
			arr[1] = url;
		} else
		if (arr.length==1) {
			String url = arr[0];
			arr = new String[2];
			arr[0] = defaultVerb;
			arr[1] = url;
		}
		arr[0] = arr[0].trim().toUpperCase();
		
		// Process placeholders
		arr[0] = processPlaceholders(arr[0], context);
		context.setProperty(scope+"-VERB", arr[0]);
		arr[1] = processPlaceholders(arr[1], context);
		context.setProperty(scope+"-URL", arr[1]);
		
		return arr;
	}
	
	protected String processPlaceholders(String str, Properties context) {
		if (str==null || context==null) return str;
		for (Object o : context.keySet()) {
			String key = (String)o;
			String val = context.getProperty(key);
			str = str.replaceAll("%\\{"+key+"\\}%", val);
		}
		return str;
	}
	
	public Configuration getConfiguration() { return config; }
	public void setConfiguration(Configuration cfg) { config = cfg; }
	
	public void startManager() {
		try {
			// Start callback endpoint
			server.startEndpoint();
			setOnline(true);
			
			// Subscribe to Pub/Sub
			for (String topicName : topics.keySet()) {
				String[] subscribeArr = topics.get(topicName)[1];
				if (subscribeArr==null) {
					logger.info("PubsubEventManager: Topic {} doesn't have subscribe URL. Ignoring subscription", topicName);
					continue;
				}
				String method = subscribeArr[0];
				String subscribeUrl = subscribeArr[1];
				if (subscribeUrl==null || subscribeUrl.trim().isEmpty()) continue;
				logger.info("PubsubEventManager: Subscribing to {} topic: URL={}", topicName, subscribeUrl);
				callRestWS(subscribeUrl, method, String.class, "");
			}
			logger.info("PubsubEventManager: Endpoint is ready");
		} catch (Exception e) {
			logger.error("PubsubEventManager.startManager: EXCEPTION CAUGHT: {}", e);
			e.printStackTrace();
		}
	}
	public void stopManager() {
		try {
			// Unsubscribe from Pub/Sub
			for (String topicName : topics.keySet()) {
				String[] unsubscribeArr = topics.get(topicName)[2];
				if (unsubscribeArr==null) {
					logger.info("PubsubEventManager: Topic {} doesn't have unsubscribe URL. Ignoring unsubscription", topicName);
					continue;
				}
				String method = unsubscribeArr[0];
				String unsubscribeUrl = unsubscribeArr[1];
				if (unsubscribeUrl==null || unsubscribeUrl.trim().isEmpty()) continue;
				logger.info("PubsubEventManager: Unsubscribing from {} topic: URL={}", topicName, unsubscribeUrl);
				callRestWS(unsubscribeUrl, method, String.class, "");
			}
			
			// Stop callback endpoint
			server.stopEndpoint();
			setOnline(false);
		} catch (Exception e) {
			logger.error("PubsubEventManager.stopManager: EXCEPTION CAUGHT: {}", e);
			e.printStackTrace();
		}
	}
	// Copied from eu.brokeratcloud.rest.gui.AbstractFacingComponent
	protected Object callRestWS(String url, String method, Class clss, Object entity) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(url);
		
		Response response = null;
		method = method.trim().toLowerCase();
		if (method.equals("get")) response = target.request().get();
		else if (method.equals("put")) response = target.request().put( Entity.json(entity) );
		else if (method.equals("post")) response = target.request().post( Entity.json(entity) );
		else if (method.equals("delete")) response = target.request().delete();
		int status = response.getStatus();
		logger.debug("--> Response: "+status);
		if (status>299) throw new RuntimeException("PubsubEventManager.callRestWS: Operation failed: Status="+status+", URL="+url);
		
		Object obj = null;
		if (method.equals("get")) {
			if (clss==null) clss = Object.class;
			obj = response.readEntity( clss );
		} else {
			obj = response.getEntity();
		}
		response.close();
		return obj;
	}
	
	public boolean isOnline() { return online; }
	protected void setOnline(boolean b) { online = b; }
	
	public void eventReceived(String evtText) {
		try {
			SLMEvent evt = SLMEvent.parseEvent(evtText);
			eventReceived(evt);
		} catch (Exception e) {
			logger.error("PubsubEventManager.eventReceived(String): Error while parsing SLM event: {}", e);
			//e.printStackTrace();
		}
	}
	
	public void eventReceived(SLMEvent evt) {
		logger.trace("Event received: id={}", evt.getId());
		if (evt.getType()==null || evt.getType().trim().isEmpty()) {
			logger.error("PubsubEventManager.eventReceived: Missing event type: Event-Id: {}", evt.getId());
			return;
		}
		
		String type = evt.getType().trim().toLowerCase();
		if ("service-onboarded".equals(type)) serviceOnboarded(evt);
		else if ("service-deprecated".equals(type)) serviceDepreciated(evt);
		else if ("service-updated".equals(type)) serviceUpdated(evt);
		else logger.error("PubsubEventManager.eventReceived: Unknown event type: {}, Event-Id: {}", evt.getType(), evt.getId());
	}
	
	public void serviceOnboarded(SLMEvent evt) {
		recomMgr.requestRecommendations(evt);
	}
	
	public void serviceDepreciated(SLMEvent evt) {
		recomMgr.requestRecommendations(evt);
	}
	
	public void serviceUpdated(SLMEvent evt) {
		recomMgr.requestRecommendations(evt);
	}
	
	public boolean publish(SLMEvent evt) {
		if (evt==null) throw new IllegalArgumentException("PubsubEventManager.publish: Null argument passed: SLMEvent evt");
		String topicName = evt.getTopic();
		if (topicName==null || (topicName=topicName.trim()).isEmpty()) throw new IllegalArgumentException("PubsubEventManager.publish: Event passed has no topic: "+evt);
		String[][] arr = topics.get(topicName);
		if (arr==null) { logger.warn("PubsubEventManager.publish: Event topic not found: {}", topicName); return false; }
		String verb = arr[3][0];
		String url = arr[3][1];
		String content = evt.getProperty(".EVENT-CONTENT");
		content = content==null ? "" : content.trim();
		url = url.replace("%{EVENT-CONTENT}%", content);
		logger.info("PubsubEventManager.publish: Publishing event using: {} {}", verb, url);
		callRestWS(url, verb, String.class, content);
		return true;
	}
}
