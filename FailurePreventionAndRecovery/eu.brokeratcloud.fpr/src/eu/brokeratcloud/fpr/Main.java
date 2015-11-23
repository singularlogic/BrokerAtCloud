package eu.brokeratcloud.fpr;

import java.io.IOException;
import java.net.URI;

import javax.jms.JMSException;
import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RecommendationHistory.INSTANCE.initSamples();

		
		// load a diva model
		// IFile file = prj.getFile("default.xmi");
		
		Repository.mainRoot = new DivaRoot(org.eclipse.emf.common.util.URI.createFileURI("./main.diva"));


		updateAndSave();

		URI uri = UriBuilder.fromUri("http://0.0.0.0/").port(8089).build();
		ResourceConfig resourceConfig = new ResourceConfig(Demo.class);
		resourceConfig.register(Recommendation.class);
		resourceConfig.register(DependencyChecking.class);
		resourceConfig.register(PubSub.class);
		resourceConfig.register(JacksonJsonProvider.class); // Using Jackson for
															// JSON wrapping
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig);
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
						// TODO Auto-generated catch block
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
