package eu.brokeratcloud.fpr;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.std.JsonValueSerializer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import eu.brokeratcloud.fpr.jms.Subscriber;
import eu.brokeratcloud.fpr.model.DivaRoot;
import eu.brokeratcloud.fpr.model.RecommendationHistory;
import eu.brokeratcloud.fpr.model.Repository;
import eu.brokeratcloud.fpr.resources.DependencyChecking;
import eu.brokeratcloud.fpr.resources.PubSub;
import eu.brokeratcloud.fpr.resources.Recommendation;

public class Main {
	
	public static List<String> inUse = new ArrayList<String>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RecommendationHistory.INSTANCE.initSamples();

		
		// load a diva model
		// IFile file = prj.getFile("default.xmi");
		
		Repository.mainRoot = new DivaRoot(org.eclipse.emf.common.util.URI.createFileURI("./main.diva"));


		updateAndSave();
		String port = PropertiesUtil.INSTANCE.get("port");
		URI uri = UriBuilder.fromUri("http://0.0.0.0/").port(Integer.valueOf(port)).build();
		ResourceConfig resourceConfig = new ResourceConfig(Demo.class);
		resourceConfig.register(Recommendation.class);
		resourceConfig.register(DependencyChecking.class);
		resourceConfig.register(PubSub.class);
		resourceConfig.register(JacksonJsonProvider.class);
		resourceConfig.register(MyExceptionMapper.class);
		// Using Jackson for
															// JSON wrapping
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig);
		//ResourceConfig.
		//resourceConfig.a
		//GrizzlyHttpServerFactory.
		try {
			server.start();
		} catch (IOException e) {

			e.printStackTrace();
		}
		// this.updateAndSave();

		new Thread() {
			@Override
			public void run() {
				while (true) {
					Repository.mainRoot.updateModel();
					// Repository.mainRoot.runSimulation();
					// Repository.configPool = new ConfigurationsPool(
					// Repository.mainRoot.getScenarios().iterator().next().getContext().get(0)
					// );
					try {
						sleep(3600000);
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		new Thread(){
			public void run(){
				while(true){
					try{
					if(Subscriber.dirty){
						Subscriber.dirty=false;
						if(inUse.isEmpty()){
							boolean empty = true;
							for(List lst : PubSub.failureRecords.values()){
								if(!lst.isEmpty()){
									inUse.addAll(lst);
								}
							}
							
						}
						Object obj = new Recommendation().getRecommendationQuery("SomeCon", inUse);
						Map m = (Map) obj;
						try{
							if(!( ((List)m.get("add")).isEmpty() && ((List) m.get("remove")).isEmpty())){
								ObjectMapper mapper = new ObjectMapper();
								String message = mapper.writeValueAsString(obj);
								Subscriber.sendMessage("FprRecommendation", message);
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					}catch (Exception e){
						System.err.println(e);
					}
					try{
						sleep(10000);
					}				
					catch (InterruptedException e) {
						e.printStackTrace();
					
					}
				}
			}
		}.start();

		
		try {
			Subscriber.startListening();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("The pub-sub server is not available at: " + Subscriber.pubsubServer);
		}
	}
	
	public static void updateAndSave() {
		DivaRoot d = Repository.mainRoot.fork();
		d.updateModel();
		d.updateOnRequest("broker", "cloud");
		d.saveModel(org.eclipse.emf.common.util.URI.createFileURI("./main-gen.diva"));
	}

}
