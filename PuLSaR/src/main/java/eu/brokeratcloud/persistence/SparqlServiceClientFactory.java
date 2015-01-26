package eu.brokeratcloud.persistence;

public class SparqlServiceClientFactory {
	protected static final String defaultClient = "eu.brokeratcloud.fuseki.FusekiClient";
	
	public static SparqlServiceClient getClientInstance() {
		String clss = defaultClient;
		try {
			return (SparqlServiceClient) Class.forName(clss).newInstance();
		} catch (Exception e) {
			System.err.println("SparqlServiceClientFactory.getClientInstance: class="+clss+": EXCEPTION THROWN: "+e);
			throw new RuntimeException(e);
		}
	}
}