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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.text.ParseException;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *	An HTTP endpoint implemention using Jetty. Accepts callbacks from Pub/Sub infrastructure
 */
public class CallbackEndpointServer extends AbstractHandler {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("eu.brokeratcloud.event.manager.pubsub");
	
	private static final String onboardedPath = "/service/onboarded";
	private static final String deprecatedPath = "/service/deprecated";
	private static final String updatedPath = "/service/updated";
	private static final String eventTpl =
								"# Received: %s\n"+
								".SLM_EVENT_TOPIC = %s\n"+
								".SLM_EVENT_TYPE  = %s\n"+
								"service-description = %s";
	protected Server server = null;
	protected String host = "127.0.0.1";
	protected int port = 8080;
	protected PubsubEventManager manager;
	protected Thread runner;		// endpoint server thread
	protected Thread runner2;		// event dispatch thread
	protected LinkedBlockingQueue<SLMEvent> queue;	// event dispatch queue
	
	public CallbackEndpointServer(PubsubEventManager mgr, HashMap<String,String[][]> t) { manager = mgr; }
	public CallbackEndpointServer(PubsubEventManager mgr, HashMap<String,String[][]> t, String host, int port) { this(mgr,t); this.host = host; this.port = port; }
	
	public void startEndpoint() throws Exception {
		if (server!=null) return;
		
		// event dispatching thread
		queue = new LinkedBlockingQueue<SLMEvent>();
		runner2 = new Thread() {
			protected LinkedBlockingQueue<SLMEvent> queue;
			public Thread setQueue(LinkedBlockingQueue<SLMEvent> q) { queue = q; return this; }
			public void run() {
				try {
					logger.info("CallbackEndpointServer: event dispatch thread started");
					boolean keepRunning = true;
					while (keepRunning) {
						SLMEvent event = queue.take();
						if (event==null || event.getType()!=null && event.getType().trim().equalsIgnoreCase("EXIT")) keepRunning = false;
						else logger.info("CallbackEndpointServer: Processing event: \n\toperation={}\n\tservice-uri={}", 
																				event.getType(), event.getProperty("service-description"));
						if (keepRunning && manager!=null) manager.eventReceived( event );
						else logger.error("CallbackEndpointServer: No manager set");
					}
					logger.info("CallbackEndpointServer: event dispatch thread terminated");
				} catch (Exception e) {
					logger.error("CallbackEndpointServer: event dispatch thread caught an exception: {}", e);
					//manager.stopManager();	// or setOnline(false);
				}
				runner2 = null;
				queue = null;
			}
		}.setQueue(queue);
		runner2.setDaemon(true);
		runner2.start();
		
		// endpoint server thread
		runner = new Thread() {
			protected CallbackEndpointServer handler;
			public Thread setHandler(CallbackEndpointServer h) { handler = h; return this; }
			public void run() {
				try {
					logger.info("CallbackEndpointServer: endpoint server thread started");
					server = new Server(port);
					server.setStopAtShutdown(true);
					server.setHandler(handler);
					server.start();
					server.join();
					logger.info("CallbackEndpointServer: endpoint server thread terminated");
					server = null;
					runner = null;
				} catch (Exception e) {
					logger.error("CallbackEndpointServer: endpoint server thread caught an exception. Closing endpoint. Exception: {}", e);
					manager.stopManager();	// or setOnline(false);
				}
			}
		}.setHandler(this);
		runner.setDaemon(true);
		runner.start();
	}
	
	public void stopEndpoint() throws Exception {
		logger.debug("CallbackEndpointServer: Closing endpoint...");
		if (server!=null) {
			server.stop();
		}
		if (runner2!=null && queue!=null) {
			SLMEvent exitEvent = new SLMEvent(".","EXIT",null);
			queue.put(exitEvent);
		}
		logger.debug("CallbackEndpointServer: endpoint closed");
	}
	
	public void handle(String target,
					   Request baseRequest,
					   HttpServletRequest request,
					   HttpServletResponse response)
		throws IOException, ServletException
	{
		// Extract information from HTTP request
		String method = request.getMethod();
		String contentType = request.getContentType();
		java.io.BufferedReader br = request.getReader();
		java.util.Scanner s = new java.util.Scanner(br).useDelimiter("\\A");
		String content = s.hasNext() ? s.next() : "";
		
		// Get operation from path (target)
		String operation = null;
		if (target.equals(onboardedPath)) operation = "service-onboarded";
		else if (target.equals(deprecatedPath)) operation = "service-deprecated";
		else if (target.equals(updatedPath)) operation = "service-updated";
		else logger.warn("CallbackEndpointServer: Invalid path: {}", target);
		
		// Extracting service URI from message body
		String eventText = null;
		if (operation!=null) {
			String srvUri = extractServiceUri(content);
			if (srvUri!=null && !srvUri.isEmpty()) {
				eventText = String.format(eventTpl, new java.util.Date().toString(), target, operation, srvUri);
			} else logger.warn("CallbackEndpointServer: Invalid event message: \n{}", content);
		}
		else logger.warn("CallbackEndpointServer: No operation specified");
		
		// Create an SLMEvent instance and push it to event dispatcher queue
		if (eventText!=null) {
			try {
				SLMEvent event = SLMEvent.parseEvent(eventText);
				logger.info("CallbackEndpointServer: Event received");
				
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				
				if (manager!=null) {
					if (queue.offer(event)) logger.debug("CallbackEndpointServer: Event queued for dispatch ({} events awaiting)", queue.size());
					else logger.error("CallbackEndpointServer: Failed to queue event in dispatch queue");
				}
				else logger.error("CallbackEndpointServer: No manager set");
				
			} catch (ParseException e) {
				logger.warn("CallbackEndpointServer: Invalid SLM event format: Event Spec:\n{}", eventText);
			}
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			baseRequest.setHandled(true);
		}
	}
	
	public String extractServiceUri(String content) {
		String srvUri = null;
		content = content.trim();
		content = content.substring(1, content.length()-1).trim();
		String part[] = content.split(":", 2);
		for (int i=0; i<part.length; i++) {
			part[i] = part[i].trim();
			part[i] = part[i].substring(1, part[i].length()-1);
		}
		if (part.length==2 && part[0].equalsIgnoreCase("serviceID")) {
			srvUri = part[1];
		}
		return srvUri;
	}
}
