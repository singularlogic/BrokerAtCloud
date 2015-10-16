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
					System.out.println("CallbackEndpointServer: event dispatch thread started");
					boolean keepRunning = true;
					while (keepRunning) {
						SLMEvent event = queue.take();
						if (event==null || event.getType()!=null && event.getType().trim().equalsIgnoreCase("EXIT")) keepRunning = false;
						else System.out.println("CallbackEndpointServer: Processing event: \n\toperation="+event.getType()+"\n\tservice-uri="+event.getProperty("service-description"));
						if (keepRunning && manager!=null) manager.eventReceived( event );
						else System.err.println("CallbackEndpointServer: No manager set");
					}
					System.out.println("\nCallbackEndpointServer: event dispatch thread terminated");
				} catch (Exception e) {
					System.err.println("CallbackEndpointServer: event dispatch thread caught an exception: "+e);
					e.printStackTrace(System.err);
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
					System.out.println("CallbackEndpointServer: endpoint server thread started");
					server = new Server(port);
					server.setStopAtShutdown(true);
					server.setHandler(handler);
					server.start();
					server.join();
					System.out.println("\nCallbackEndpointServer: endpoint server thread terminated");
					server = null;
					runner = null;
				} catch (Exception e) {
					System.err.println("CallbackEndpointServer: endpoint server thread caught an exception. Closing endpoint. Exception: "+e);
					e.printStackTrace(System.err);
					manager.stopManager();	// or setOnline(false);
				}
			}
		}.setHandler(this);
		runner.setDaemon(true);
		runner.start();
	}
	
	public void stopEndpoint() throws Exception {
		System.out.println("CallbackEndpointServer: Closing endpoint...");
		if (server!=null) {
			server.stop();
		}
		if (runner2!=null && queue!=null) {
			SLMEvent exitEvent = new SLMEvent(".","EXIT",null);
			queue.put(exitEvent);
		}
		System.out.println("CallbackEndpointServer: endpoint closed");
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
		else System.err.println("CallbackEndpointServer: Invalid path: "+target);
		
		// Extracting service URI from message body
		String eventText = null;
		if (operation!=null) {
			String srvUri = extractServiceUri(content);
			if (srvUri!=null && !srvUri.isEmpty()) {
				eventText = String.format(eventTpl, new java.util.Date().toString(), target, operation, srvUri);
			} else System.err.println("CallbackEndpointServer: Invalid event message: \n"+content);
		}
		else System.err.println("CallbackEndpointServer: No operation specified");
		
		// Create an SLMEvent instance and push it to event dispatcher queue
		if (eventText!=null) {
			try {
				SLMEvent event = SLMEvent.parseEvent(eventText);
				System.out.println("CallbackEndpointServer: Event received");
				
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				
				if (manager!=null) {
					String m = queue.offer(event) ? "CallbackEndpointServer: Event queued for dispatch ("+queue.size()+" events awaiting)" : "CallbackEndpointServer: Failed to queue event in dispatch queue";
					System.out.println(m);
				}
				else System.err.println("CallbackEndpointServer: No manager set");
				
			} catch (ParseException e) {
				System.err.println("CallbackEndpointServer: Invalid SLM event format: Event Spec:\n"+eventText);
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
